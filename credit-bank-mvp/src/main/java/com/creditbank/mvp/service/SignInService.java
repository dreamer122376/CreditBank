package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 签到服务。
 * 通过 Redis 缓存今日签到状态以避免重复签到，每日签到奖励积分，
 * 并统计连续签到天数（最高追溯 365 天）。
 * 额外注释
 */
@Service
public class SignInService {

    /** 签到事件编码，对应积分规则表中 ATTENDANCE 规则 */
    private static final String SIGN_IN_EVENT_CODE = "ATTENDANCE";

    private final SysUserMapper sysUserMapper;
    private final CreditRuleMapper creditRuleMapper;
    private final TransactionLogMapper transactionLogMapper;
    private final RedisService redisService;
    private final UserOpLogMapper userOpLogMapper;

    public SignInService(SysUserMapper sysUserMapper,
                         CreditRuleMapper creditRuleMapper,
                         TransactionLogMapper transactionLogMapper,
                         RedisService redisService,
                         UserOpLogMapper userOpLogMapper) {
        this.sysUserMapper = sysUserMapper;
        this.creditRuleMapper = creditRuleMapper;
        this.transactionLogMapper = transactionLogMapper;
        this.redisService = redisService;
        this.userOpLogMapper = userOpLogMapper;
    }

    /**
     * 签到操作。先查 Redis 缓存判断今日是否已签到，再查数据库二次校验，
     * 通过后加积分、写交易日志、更新 Redis 缓存。
     *
     * @param userId 用户 ID
     * @return 签到结果（成功标志、获得积分、新余额、连续签到天数）
     */
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> signIn(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        String todayKey = "sign:today:" + userId;
        Object cached = redisService.get(todayKey);
        if (cached instanceof Boolean && (Boolean) cached) {
            throw new BizException("今日已签到");
        }

        CreditRule signInRule = getSignInRule();
        if (signInRule == null) {
            throw new BizException("签到规则不存在或已停用");
        }
        Long signInRuleId = signInRule.getId();

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        long todaySignInCount = transactionLogMapper.selectCount(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getUserId, userId)
                        .eq(TransactionLog::getBizType, "DAILY")
                        .eq(TransactionLog::getRelatedRuleId, signInRuleId)
                        .ge(TransactionLog::getCreatedAt, todayStart)
                        .le(TransactionLog::getCreatedAt, todayEnd));

        if (todaySignInCount > 0) {
            throw new BizException("今日已签到");
        }

        int creditValue = signInRule.getCreditValue();
        int newBalance = user.getBalance() + creditValue;
        user.setBalance(newBalance);
        sysUserMapper.updateById(user);

        TransactionLog txn = new TransactionLog();
        txn.setUserId(userId);
        txn.setAmount(creditValue);
        txn.setBalanceAfter(newBalance);
        txn.setBizType("DAILY");
        txn.setRelatedRuleId(signInRuleId);
        txn.setDescription("签到打卡");
        transactionLogMapper.insert(txn);

        // 标记今日已签到（过期时间为当天结束）
        long secondsUntilEndOfDay = ChronoUnit.SECONDS.between(
                LocalDateTime.now(), today.atTime(LocalTime.MAX));
        redisService.set(todayKey, true, secondsUntilEndOfDay, TimeUnit.SECONDS);

        int streak = calculateStreak(userId);
        redisService.set("sign:streak:" + userId, streak, secondsUntilEndOfDay, TimeUnit.SECONDS);

        userOpLogMapper.insert(UserOpLog.createLog(
                userId, user.getRealName(), userId, user.getRealName(),
                UserOpLog.MODULE_POINT, UserOpLog.ACTION_EARN,
                "签到打卡获得积分 " + creditValue + "，连续签到 " + streak + " 天"));

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("creditEarned", creditValue);
        result.put("newBalance", newBalance);
        result.put("streak", streak);
        result.put("message", "签到成功！连续签到 " + streak + " 天");

        return result;
    }

    public Map<String, Object> getSignInStatus(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        String todayKey = "sign:today:" + userId;
        Object cachedToday = redisService.get(todayKey);
        boolean hasSignedIn;

        CreditRule signInRule = getSignInRule();
        if (signInRule == null) {
            throw new BizException("签到规则不存在或已停用");
        }
        Long signInRuleId = signInRule.getId();

        if (cachedToday instanceof Boolean) {
            hasSignedIn = (Boolean) cachedToday;
        } else {
            LocalDate today = LocalDate.now();
            long todaySignInCount = transactionLogMapper.selectCount(
                    new LambdaQueryWrapper<TransactionLog>()
                            .eq(TransactionLog::getUserId, userId)
                            .eq(TransactionLog::getBizType, "DAILY")
                            .eq(TransactionLog::getRelatedRuleId, signInRuleId)
                            .ge(TransactionLog::getCreatedAt, today.atStartOfDay())
                            .le(TransactionLog::getCreatedAt, today.atTime(LocalTime.MAX)));
            hasSignedIn = todaySignInCount > 0;
            if (hasSignedIn) {
                long expire = ChronoUnit.SECONDS.between(
                        LocalDateTime.now(), today.atTime(LocalTime.MAX));
                redisService.set(todayKey, true, expire, TimeUnit.SECONDS);
            }
        }

        String streakKey = "sign:streak:" + userId;
        Object cachedStreak = redisService.get(streakKey);
        int streak;
        if (cachedStreak instanceof Integer) {
            streak = (Integer) cachedStreak;
        } else {
            streak = calculateStreak(userId);
            LocalDate today = LocalDate.now();
            long expire = ChronoUnit.SECONDS.between(
                    LocalDateTime.now(), today.atTime(LocalTime.MAX));
            redisService.set(streakKey, streak, expire, TimeUnit.SECONDS);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasSignedIn", hasSignedIn);
        result.put("streak", streak);
        result.put("currentBalance", user.getBalance());
        result.put("signInCredit", signInRule.getCreditValue());

        return result;
    }

    public List<TransactionLog> getSignInHistory(Long userId, int limit) {
        CreditRule signInRule = getSignInRule();
        if (signInRule == null) {
            return Collections.emptyList();
        }
        Long signInRuleId = signInRule.getId();
        
        return transactionLogMapper.selectList(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getUserId, userId)
                        .eq(TransactionLog::getBizType, "DAILY")
                        .eq(TransactionLog::getRelatedRuleId, signInRuleId)
                        .orderByDesc(TransactionLog::getCreatedAt)
                        .last("LIMIT " + limit));
    }

    private int calculateStreak(Long userId) {
        CreditRule signInRule = getSignInRule();
        if (signInRule == null) {
            return 0;
        }
        Long signInRuleId = signInRule.getId();

        LocalDate today = LocalDate.now();
        LocalDateTime thirtyDaysAgo = today.minusDays(365).atStartOfDay();

        List<TransactionLog> signInRecords = transactionLogMapper.selectList(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getUserId, userId)
                        .eq(TransactionLog::getBizType, "DAILY")
                        .eq(TransactionLog::getRelatedRuleId, signInRuleId)
                        .ge(TransactionLog::getCreatedAt, thirtyDaysAgo)
                        .orderByDesc(TransactionLog::getCreatedAt));

        Set<LocalDate> signInDates = signInRecords.stream()
                .map(t -> t.getCreatedAt().toLocalDate())
                .collect(Collectors.toSet());

        int streak = 0;
        LocalDate checkDate = today;

        while (signInDates.contains(checkDate)) {
            streak++;
            checkDate = checkDate.minusDays(1);
        }

        return streak;
    }

    /**
     * 获取已启用的签到积分规则
     */
    private CreditRule getSignInRule() {
        return creditRuleMapper.selectOne(
                new LambdaQueryWrapper<CreditRule>()
                        .eq(CreditRule::getEventCode, SIGN_IN_EVENT_CODE)
                        .eq(CreditRule::getIsEnabled, 1));
    }
}