package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.ExchangeRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.ExchangeRuleMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExchangeRuleService {

    private final ExchangeRuleMapper exchangeRuleMapper;
    private final SysUserMapper sysUserMapper;
    private final TransactionLogMapper transactionLogMapper;
    private final UserOpLogMapper userOpLogMapper;

    public ExchangeRuleService(ExchangeRuleMapper exchangeRuleMapper,
                               SysUserMapper sysUserMapper,
                               TransactionLogMapper transactionLogMapper,
                               UserOpLogMapper userOpLogMapper) {
        this.exchangeRuleMapper = exchangeRuleMapper;
        this.sysUserMapper = sysUserMapper;
        this.transactionLogMapper = transactionLogMapper;
        this.userOpLogMapper = userOpLogMapper;
    }

    public List<ExchangeRule> list() {
        return exchangeRuleMapper.selectList(
                new LambdaQueryWrapper<ExchangeRule>().orderByDesc(ExchangeRule::getId));
    }

    public ExchangeRule create(ExchangeRule rule) {
        if (rule.getItemName() == null || rule.getItemName().trim().isEmpty()) {
            throw new BizException("兑换品名称不能为空");
        }
        if (rule.getRequiredCredit() == null || rule.getRequiredCredit() <= 0) {
            throw new BizException("所需积分必须为正数");
        }
        rule.setId(null);
        if (rule.getIsEnabled() == null) {
            rule.setIsEnabled(1);
        }
        exchangeRuleMapper.insert(rule);
        return exchangeRuleMapper.selectById(rule.getId());
    }

    public ExchangeRule update(ExchangeRule rule) {
        ExchangeRule exist = exchangeRuleMapper.selectById(rule.getId());
        if (exist == null) {
            throw new BizException("转换规则不存在：" + rule.getId());
        }
        if (rule.getRequiredCredit() != null && rule.getRequiredCredit() <= 0) {
            throw new BizException("所需积分必须为正数");
        }
        exchangeRuleMapper.updateById(rule);
        return exchangeRuleMapper.selectById(rule.getId());
    }

    public ExchangeRule toggle(Long id) {
        ExchangeRule exist = exchangeRuleMapper.selectById(id);
        if (exist == null) {
            throw new BizException("转换规则不存在：" + id);
        }
        exist.setIsEnabled(exist.getIsEnabled() != null && exist.getIsEnabled() == 1 ? 0 : 1);
        exchangeRuleMapper.updateById(exist);
        return exist;
    }

    @Transactional(rollbackFor = Exception.class)
    public ExchangeRule exchange(Long userId, Long ruleId) {
        ExchangeRule rule = exchangeRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BizException("转换规则不存在：" + ruleId);
        }
        if (rule.getIsEnabled() == null || rule.getIsEnabled() != 1) {
            throw new BizException("该兑换品已停用");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在：" + userId);
        }

        if (rule.getStock() != null && rule.getStock() <= 0) {
            throw new BizException("该兑换品库存不足");
        }

        if (user.getBalance() == null || user.getBalance() < rule.getRequiredCredit()) {
            throw new BizException("积分不足，当前余额：" + user.getBalance() + "，需要：" + rule.getRequiredCredit());
        }

        if (rule.getPerUserLimit() != null && rule.getPerUserLimit() > 0) {
            int exchangedCount = transactionLogMapper.selectCount(
                    new LambdaQueryWrapper<TransactionLog>()
                            .eq(TransactionLog::getUserId, userId)
                            .eq(TransactionLog::getBizType, "EXCHANGE")
                            .eq(TransactionLog::getRelatedRuleId, ruleId));
            if (exchangedCount >= rule.getPerUserLimit()) {
                throw new BizException("每人限兑换 " + rule.getPerUserLimit() + " 次");
            }
        }

        Integer newBalance = user.getBalance() - rule.getRequiredCredit();
        user.setBalance(newBalance);
        sysUserMapper.updateById(user);

        if (rule.getStock() != null) {
            rule.setStock(rule.getStock() - 1);
            exchangeRuleMapper.updateById(rule);
        }

        TransactionLog txn = new TransactionLog();
        txn.setUserId(userId);
        txn.setAmount(-rule.getRequiredCredit());
        txn.setBalanceAfter(newBalance);
        txn.setBizType("EXCHANGE");
        txn.setRelatedRuleId(ruleId);
        txn.setDescription("兑换「" + rule.getItemName() + "」");
        transactionLogMapper.insert(txn);

        userOpLogMapper.insert(UserOpLog.createLog(
                userId, user.getRealName(), userId, user.getRealName(),
                UserOpLog.MODULE_POINT, "EXCHANGE",
                "用户「" + user.getRealName() + "」兑换「" + rule.getItemName() + "」，消耗 " + rule.getRequiredCredit() + " 积分，当前余额：" + newBalance));

        return exchangeRuleMapper.selectById(ruleId);
    }
}
