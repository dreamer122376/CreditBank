package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SignInService {

    private static final String SIGN_IN_EVENT_CODE = "ATTENDANCE";

    private final SysUserMapper sysUserMapper;
    private final CreditRuleMapper creditRuleMapper;
    private final TransactionLogMapper transactionLogMapper;

    public SignInService(SysUserMapper sysUserMapper,
                         CreditRuleMapper creditRuleMapper,
                         TransactionLogMapper transactionLogMapper) {
        this.sysUserMapper = sysUserMapper;
        this.creditRuleMapper = creditRuleMapper;
        this.transactionLogMapper = transactionLogMapper;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> signIn(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }

        Long signInRuleId = getSignInRuleId();
        if (signInRuleId == null) {
            throw new BizException("签到规则不存在或已停用");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        long todaySignInCount = transactionLogMapper.selectCount(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getUserId, userId)
                        .eq(TransactionLog::getBizType, "REWARD")
                        .eq(TransactionLog::getRelatedRuleId, signInRuleId)
                        .ge(TransactionLog::getCreatedAt, todayStart)
                        .le(TransactionLog::getCreatedAt, todayEnd));

        if (todaySignInCount > 0) {
            throw new BizException("今日已签到");
        }

        CreditRule rule = creditRuleMapper.selectById(signInRuleId);
        int creditValue = rule.getCreditValue();
        int newBalance = user.getBalance() + creditValue;
        user.setBalance(newBalance);
        sysUserMapper.updateById(user);

        TransactionLog txn = new TransactionLog();
        txn.setUserId(userId);
        txn.setAmount(creditValue);
        txn.setBalanceAfter(newBalance);
        txn.setBizType("REWARD");
        txn.setRelatedRuleId(rule.getId());
        txn.setDescription("签到打卡");
        transactionLogMapper.insert(txn);

        int streak = calculateStreak(userId);

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

        Long signInRuleId = getSignInRuleId();
        if (signInRuleId == null) {
            throw new BizException("签到规则不存在或已停用");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        long todaySignInCount = transactionLogMapper.selectCount(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getUserId, userId)
                        .eq(TransactionLog::getBizType, "REWARD")
                        .eq(TransactionLog::getRelatedRuleId, signInRuleId)
                        .ge(TransactionLog::getCreatedAt, todayStart)
                        .le(TransactionLog::getCreatedAt, todayEnd));

        int streak = calculateStreak(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("hasSignedIn", todaySignInCount > 0);
        result.put("streak", streak);
        result.put("currentBalance", user.getBalance());
        result.put("signInCredit", getSignInCreditValue());

        return result;
    }

    public List<TransactionLog> getSignInHistory(Long userId, int limit) {
        Long signInRuleId = getSignInRuleId();
        if (signInRuleId == null) {
            return Collections.emptyList();
        }
        
        return transactionLogMapper.selectList(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getUserId, userId)
                        .eq(TransactionLog::getBizType, "REWARD")
                        .eq(TransactionLog::getRelatedRuleId, signInRuleId)
                        .orderByDesc(TransactionLog::getCreatedAt)
                        .last("LIMIT " + limit));
    }

    private int calculateStreak(Long userId) {
        Long signInRuleId = getSignInRuleId();
        if (signInRuleId == null) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDateTime thirtyDaysAgo = today.minusDays(365).atStartOfDay();

        List<TransactionLog> signInRecords = transactionLogMapper.selectList(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getUserId, userId)
                        .eq(TransactionLog::getBizType, "REWARD")
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

    private Long getSignInRuleId() {
        CreditRule rule = creditRuleMapper.selectOne(
                new LambdaQueryWrapper<CreditRule>()
                        .eq(CreditRule::getEventCode, SIGN_IN_EVENT_CODE)
                        .eq(CreditRule::getIsEnabled, 1));
        return rule != null ? rule.getId() : null;
    }

    private int getSignInCreditValue() {
        CreditRule rule = creditRuleMapper.selectOne(
                new LambdaQueryWrapper<CreditRule>()
                        .eq(CreditRule::getEventCode, SIGN_IN_EVENT_CODE)
                        .eq(CreditRule::getIsEnabled, 1));
        return rule != null ? rule.getCreditValue() : 10;
    }
}