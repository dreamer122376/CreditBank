package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.Campaign;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PointService {

    private final SysUserMapper sysUserMapper;
    private final CreditRuleMapper creditRuleMapper;
    private final TransactionLogMapper transactionLogMapper;
    private final CampaignService campaignService;

    public PointService(SysUserMapper sysUserMapper,
                        CreditRuleMapper creditRuleMapper,
                        TransactionLogMapper transactionLogMapper,
                        CampaignService campaignService) {
        this.sysUserMapper = sysUserMapper;
        this.creditRuleMapper = creditRuleMapper;
        this.transactionLogMapper = transactionLogMapper;
        this.campaignService = campaignService;
    }

    public SysUser login(String username, String password) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username)
                        .eq(SysUser::getStatus, 1));
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            throw new BizException("密码错误");
        }
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public SysUser earn(Long userId, String eventCode) {
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

        // 活动倍率加成
        int finalCredit = rule.getCreditValue();
        String campaignDesc = "";
        Campaign activeMultiplierCampaign = campaignService.getActiveMultiplierCampaign();
        if (activeMultiplierCampaign != null) {
            BigDecimal multiplied = BigDecimal.valueOf(rule.getCreditValue())
                    .multiply(activeMultiplierCampaign.getMultiplier());
            finalCredit = multiplied.setScale(0, RoundingMode.HALF_UP).intValue();
            campaignDesc = "（活动翻倍 ×" + activeMultiplierCampaign.getMultiplier() + "）";
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
        txn.setBizType("REWARD");
        txn.setDescription(rule.getEventName() + campaignDesc);
        transactionLogMapper.insert(txn);

        return sysUserMapper.selectById(userId);
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

    public List<CreditRule> listRules() {
        return creditRuleMapper.selectList(
                new LambdaQueryWrapper<CreditRule>().eq(CreditRule::getIsEnabled, 1));
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
        user.setPassword(password);
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
