package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.*;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PointService {

    private final SysUserMapper sysUserMapper;
    private final CreditRuleMapper creditRuleMapper;
    private final TransactionLogMapper transactionLogMapper;
    private final UserOpLogMapper userOpLogMapper;
    private final CampaignService campaignService;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    public PointService(SysUserMapper sysUserMapper,
                        CreditRuleMapper creditRuleMapper,
                        TransactionLogMapper transactionLogMapper,
                        UserOpLogMapper userOpLogMapper,
                        CampaignService campaignService,
                        PasswordEncoder passwordEncoder,
                        RedisService redisService) {
        this.sysUserMapper = sysUserMapper;
        this.creditRuleMapper = creditRuleMapper;
        this.transactionLogMapper = transactionLogMapper;
        this.userOpLogMapper = userOpLogMapper;
        this.campaignService = campaignService;
        this.passwordEncoder = passwordEncoder;
        this.redisService = redisService;
    }

    public SysUser login(String username, String password) {
        // 登录限流：连续失败 5 次后锁定 1 分钟
        String failKey = "login:fail:" + username;
        Object failCount = redisService.get(failKey);
        if (failCount instanceof Integer && (Integer) failCount >= 5) {
            long ttl = redisService.getExpire(failKey, TimeUnit.SECONDS);
            throw new BizException("登录失败次数过多，请 " + ttl + " 秒后再试");
        }

        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));
        if (user == null) {
            redisService.increment(failKey);
            if (redisService.getExpire(failKey) <= 0) {
                redisService.expire(failKey, 1, TimeUnit.MINUTES);
            }
            throw new BizException("用户不存在");
        }
        // 冻结用户允许登录，读写权限由 FreezePermissionInterceptor 控制
        if (!passwordEncoder.matches(password, user.getPassword())) {
            redisService.increment(failKey);
            if (redisService.getExpire(failKey) <= 0) {
                redisService.expire(failKey, 1, TimeUnit.MINUTES);
            }
            throw new BizException("密码错误");
        }

        // 登录成功，清除失败计数
        redisService.delete(failKey);

        user.setLastLoginAt(java.time.LocalDateTime.now());
        sysUserMapper.updateById(user);
        return user;
    }

    // [TEST-ONLY] 测试用跳过密码登录
    public SysUser testLogin(String username) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setLastLoginAt(java.time.LocalDateTime.now());
        sysUserMapper.updateById(user);
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUser earn(Long userId, String eventCode, Long operatorId) {
        return earn(userId, eventCode, operatorId, null, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUser earn(Long userId, String eventCode, Long operatorId, Integer customCreditValue) {
        return earn(userId, eventCode, operatorId, customCreditValue, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUser earn(Long userId, String eventCode, Long operatorId, Integer customCreditValue, String remark) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在：" + userId);
        }

        CreditRule rule = creditRuleMapper.selectOne(
                new LambdaQueryWrapper<CreditRule>()
                        .eq(CreditRule::getEventCode, eventCode)
                        .eq(CreditRule::getIsEnabled, 1));
        if (rule == null) {
            throw new BizException("积分规则不存在或已停用：" + eventCode);
        }

        boolean isAdminRule = "ADMIN".equals(eventCode);

        int finalCredit;
        String campaignDesc = "";
        if (isAdminRule) {
            if (customCreditValue == null || customCreditValue <= 0) {
                throw new BizException("管理员手动加分必须指定积分值");
            }
            if (customCreditValue > 5000) {
                throw new BizException("单次加分不能超过5000");
            }
            finalCredit = customCreditValue;
        } else {
            finalCredit = rule.getCreditValue();
            Campaign enrolledCampaign = campaignService.getEnrolledMultiplierCampaign(userId);
            if (enrolledCampaign != null) {
                BigDecimal multiplied = BigDecimal.valueOf(rule.getCreditValue())
                        .multiply(enrolledCampaign.getMultiplier());
                finalCredit = multiplied.setScale(0, RoundingMode.HALF_UP).intValue();
                campaignDesc = "（活动翻倍 ×" + enrolledCampaign.getMultiplier() + "）";
            }
        }

        Integer newBalance = user.getBalance() + finalCredit;
        user.setBalance(newBalance);
        int rows = sysUserMapper.updateById(user);
        if (rows == 0) {
            throw new BizException("用户数据更新失败");
        }

        TransactionLog txn = new TransactionLog();
        txn.setUserId(userId);
        txn.setAmount(finalCredit);
        txn.setBalanceAfter(newBalance);
        txn.setBizType(isAdminRule ? "ADMIN" : "REWARD");
        txn.setRelatedRuleId(rule.getId());
        txn.setDescription(isAdminRule ? "管理员手动加分" : (rule.getEventName() + campaignDesc));
        transactionLogMapper.insert(txn);

        Long realOperatorId = operatorId != null ? operatorId : userId;
        SysUser operator = sysUserMapper.selectById(realOperatorId);
        String operatorName = operator != null ? operator.getRealName() : String.valueOf(realOperatorId);
        String detail = "为用户「" + user.getRealName() + "」增加 " + finalCredit + " 积分，规则：" + (isAdminRule ? "管理员手动加分" : (rule.getEventName() + campaignDesc)) + "，当前余额：" + newBalance;
        if (isAdminRule && remark != null && !remark.trim().isEmpty()) {
            detail += "，说明：" + remark.trim();
        }
        userOpLogMapper.insert(UserOpLog.createLog(
                realOperatorId, operatorName, userId, user.getRealName(),
                UserOpLog.MODULE_POINT, UserOpLog.ACTION_EARN, detail));

        if (!isAdminRule && rule.getOrgId() != null) {
            SysUser orgAdmin = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getOrgId, rule.getOrgId())
                            .eq(SysUser::getRole, "org_admin")
                            .last("LIMIT 1"));
            if (orgAdmin != null) {
                int orgNewBalance = orgAdmin.getBalance() - finalCredit;
                orgAdmin.setBalance(orgNewBalance);
                sysUserMapper.updateById(orgAdmin);

                TransactionLog orgTxn = new TransactionLog();
                orgTxn.setUserId(orgAdmin.getId());
                orgTxn.setAmount(-finalCredit);
                orgTxn.setBalanceAfter(orgNewBalance);
                orgTxn.setBizType("ATTACHMENT");
                orgTxn.setRelatedRuleId(txn.getId());
                orgTxn.setDescription("学生获得积分，机构积分池扣减");
                transactionLogMapper.insert(orgTxn);
            }
        }

        return sysUserMapper.selectById(userId);
    }

    /**
     * 项目完成奖励：发放 project.credit_reward，支持活动倍率加成。
     */
    @Transactional(rollbackFor = Exception.class)
    public SysUser rewardProjectCompletion(Long studentId, Project project, Long operatorId) {
        if (project == null || project.getCreditReward() == null || project.getCreditReward() <= 0) {
            return null;
        }

        SysUser student = sysUserMapper.selectById(studentId);
        if (student == null) {
            throw new BizException("学生不存在：" + studentId);
        }

        int baseCredit = project.getCreditReward();
        int finalCredit = baseCredit;
        String campaignDesc = "";
        Campaign enrolledCampaign = campaignService.getEnrolledMultiplierCampaign(studentId);
        if (enrolledCampaign != null) {
            BigDecimal multiplied = BigDecimal.valueOf(baseCredit)
                    .multiply(enrolledCampaign.getMultiplier());
            finalCredit = multiplied.setScale(0, RoundingMode.HALF_UP).intValue();
            campaignDesc = "（活动翻倍 ×" + enrolledCampaign.getMultiplier() + "）";
        }

        Integer newBalance = student.getBalance() + finalCredit;
        student.setBalance(newBalance);
        sysUserMapper.updateById(student);

        TransactionLog txn = new TransactionLog();
        txn.setUserId(studentId);
        txn.setAmount(finalCredit);
        txn.setBalanceAfter(newBalance);
        txn.setBizType("REWARD");
        txn.setDescription("完成项目「" + project.getName() + "」获得积分" + campaignDesc);
        transactionLogMapper.insert(txn);

        Long realOperatorId = operatorId != null ? operatorId : studentId;
        SysUser operator = sysUserMapper.selectById(realOperatorId);
        String operatorName = operator != null ? operator.getRealName() : String.valueOf(realOperatorId);
        userOpLogMapper.insert(UserOpLog.createLog(
                realOperatorId, operatorName, studentId, student.getRealName(),
                UserOpLog.MODULE_POINT, UserOpLog.ACTION_EARN,
                "学生「" + student.getRealName() + "」完成项目「" + project.getName() + "」获得 " + finalCredit + " 积分" + campaignDesc + "，当前余额：" + newBalance));

        if (project.getOrgId() != null) {
            SysUser orgAdmin = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getOrgId, project.getOrgId())
                            .eq(SysUser::getRole, "org_admin")
                            .last("LIMIT 1"));
            if (orgAdmin != null) {
                int orgNewBalance = orgAdmin.getBalance() - finalCredit;
                orgAdmin.setBalance(orgNewBalance);
                sysUserMapper.updateById(orgAdmin);

                TransactionLog orgTxn = new TransactionLog();
                orgTxn.setUserId(orgAdmin.getId());
                orgTxn.setAmount(-finalCredit);
                orgTxn.setBalanceAfter(orgNewBalance);
                orgTxn.setBizType("REWARD");
                orgTxn.setDescription("学生完成项目，机构积分池扣减");
                transactionLogMapper.insert(orgTxn);
            }
        }

        return sysUserMapper.selectById(studentId);
    }

    public SysUser getUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在：" + id);
        }
        return user;
    }

    public List<SysUser> listUsers() {
        return sysUserMapper.selectList(null);
    }

    public List<TransactionLog> listTransactions(Long userId) {
        return transactionLogMapper.selectList(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getUserId, userId)
                        .orderByDesc(TransactionLog::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUser register(String username, String password, String realName, String role, Long orgId, String expertField) {
        SysUser existing = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));
        if (existing != null) {
            throw new BizException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRealName(realName);
        user.setRole(role);
        user.setOrgId(orgId);
        user.setExpertField(expertField);
        user.setBalance(0);
        user.setStatus(1);
        user.setCreatedAt(java.time.LocalDateTime.now());

        sysUserMapper.insert(user);
        return user;
    }
}
