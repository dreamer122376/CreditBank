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

import java.util.List;

@Service
public class PointService {

    private final SysUserMapper sysUserMapper;
    private final CreditRuleMapper creditRuleMapper;
    private final TransactionLogMapper transactionLogMapper;

    public PointService(SysUserMapper sysUserMapper,
                        CreditRuleMapper creditRuleMapper,
                        TransactionLogMapper transactionLogMapper) {
        this.sysUserMapper = sysUserMapper;
        this.creditRuleMapper = creditRuleMapper;
        this.transactionLogMapper = transactionLogMapper;
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

        Integer newBalance = user.getBalance() + rule.getCreditValue();
        user.setBalance(newBalance);
        int rows = sysUserMapper.updateById(user);
        if (rows == 0) {
            throw new BizException("用户数据更新失败");
        }

        TransactionLog txn = new TransactionLog();
        txn.setUserId(userId);
        txn.setAmount(rule.getCreditValue());
        txn.setBalanceAfter(newBalance);
        txn.setBizType("REWARD");
        txn.setDescription(rule.getEventName());
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
}
