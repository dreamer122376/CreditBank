package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.ExchangeRule;
import com.creditbank.mvp.mapper.ExchangeRuleMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExchangeRuleService {

    private final ExchangeRuleMapper exchangeRuleMapper;

    public ExchangeRuleService(ExchangeRuleMapper exchangeRuleMapper) {
        this.exchangeRuleMapper = exchangeRuleMapper;
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
}
