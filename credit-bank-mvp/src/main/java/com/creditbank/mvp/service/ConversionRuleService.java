package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.ConversionRule;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.ConversionRuleMapper;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConversionRuleService {

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    private final ConversionRuleMapper conversionRuleMapper;
    private final OrganizationMapper organizationMapper;
    private final CreditRuleMapper creditRuleMapper;
    private final SysUserMapper sysUserMapper;

    public ConversionRuleService(ConversionRuleMapper conversionRuleMapper,
                                 OrganizationMapper organizationMapper,
                                 CreditRuleMapper creditRuleMapper,
                                 SysUserMapper sysUserMapper) {
        this.conversionRuleMapper = conversionRuleMapper;
        this.organizationMapper = organizationMapper;
        this.creditRuleMapper = creditRuleMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public List<ConversionRule> list() {
        return enrichWithRelatedData(conversionRuleMapper.selectList(
                new LambdaQueryWrapper<ConversionRule>().orderByDesc(ConversionRule::getId)));
    }

    public List<ConversionRule> listByEnabled(boolean enabled) {
        return enrichWithRelatedData(conversionRuleMapper.selectList(
                new LambdaQueryWrapper<ConversionRule>()
                        .eq(ConversionRule::getIsEnabled, enabled ? STATUS_ENABLED : STATUS_DISABLED)
                        .orderByDesc(ConversionRule::getId)));
    }

    public List<ConversionRule> listByOrgId(Long orgId) {
        return enrichWithRelatedData(conversionRuleMapper.selectList(
                new LambdaQueryWrapper<ConversionRule>()
                        .and(wrapper -> wrapper
                                .isNull(ConversionRule::getConvertedOrgId)
                                .or()
                                .eq(ConversionRule::getConvertedOrgId, orgId))
                        .orderByDesc(ConversionRule::getId)));
    }

    public List<ConversionRule> listByOriginalType(String originalType) {
        return enrichWithRelatedData(conversionRuleMapper.selectList(
                new LambdaQueryWrapper<ConversionRule>()
                        .eq(ConversionRule::getOriginalType, originalType)
                        .orderByDesc(ConversionRule::getId)));
    }

    public ConversionRule getById(Long id) {
        ConversionRule rule = conversionRuleMapper.selectById(id);
        if (rule == null) {
            throw new BizException("转换规则不存在：" + id);
        }
        return enrichWithRelatedData(List.of(rule)).get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    public ConversionRule create(ConversionRule rule) {
        if (rule.getOriginalName() == null || rule.getOriginalName().trim().isEmpty()) {
            throw new BizException("原成果名称不能为空");
        }
        if (rule.getConvertedName() == null || rule.getConvertedName().trim().isEmpty()) {
            throw new BizException("转换后成果名称不能为空");
        }
        if (rule.getOriginalType() == null || rule.getOriginalType().trim().isEmpty()) {
            throw new BizException("原成果类型不能为空");
        }
        if (rule.getConvertedType() == null || rule.getConvertedType().trim().isEmpty()) {
            throw new BizException("转换后成果类型不能为空");
        }
        if (rule.getCreditRuleId() != null) {
            CreditRule creditRule = creditRuleMapper.selectById(rule.getCreditRuleId());
            if (creditRule == null) {
                throw new BizException("积分规则不存在：" + rule.getCreditRuleId());
            }
        }
        if (rule.getOriginalOrgId() != null) {
            Organization org = organizationMapper.selectById(rule.getOriginalOrgId());
            if (org == null) {
                throw new BizException("原成果机构不存在：" + rule.getOriginalOrgId());
            }
        }
        if (rule.getConvertedOrgId() != null) {
            Organization org = organizationMapper.selectById(rule.getConvertedOrgId());
            if (org == null) {
                throw new BizException("转换后成果机构不存在：" + rule.getConvertedOrgId());
            }
        }
        if (rule.getEffectiveStart() != null && rule.getEffectiveEnd() != null) {
            if (rule.getEffectiveStart().isAfter(rule.getEffectiveEnd())) {
                throw new BizException("开始时间不能晚于结束时间");
            }
        }
        rule.setId(null);
        rule.setCreatedAt(LocalDateTime.now());
        conversionRuleMapper.insert(rule);
        return getById(rule.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ConversionRule update(ConversionRule rule) {
        ConversionRule exist = getById(rule.getId());
        if (rule.getCreditRuleId() != null) {
            CreditRule creditRule = creditRuleMapper.selectById(rule.getCreditRuleId());
            if (creditRule == null) {
                throw new BizException("积分规则不存在：" + rule.getCreditRuleId());
            }
        }
        if (rule.getEffectiveStart() != null && rule.getEffectiveEnd() != null) {
            if (rule.getEffectiveStart().isAfter(rule.getEffectiveEnd())) {
                throw new BizException("开始时间不能晚于结束时间");
            }
        }
        rule.setCreatedAt(exist.getCreatedAt());
        rule.setCreatedBy(exist.getCreatedBy());
        conversionRuleMapper.updateById(rule);
        return getById(rule.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getById(id);
        conversionRuleMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public ConversionRule toggleEnabled(Long id, Integer isEnabled) {
        ConversionRule exist = getById(id);
        if (isEnabled == null || (isEnabled != STATUS_ENABLED && isEnabled != STATUS_DISABLED)) {
            throw new BizException("非法的状态值：" + isEnabled);
        }
        exist.setIsEnabled(isEnabled);
        conversionRuleMapper.updateById(exist);
        return exist;
    }

    private List<ConversionRule> enrichWithRelatedData(List<ConversionRule> rules) {
        if (rules.isEmpty()) {
            return rules;
        }

        Map<Long, String> orgNames = organizationMapper.selectList(null).stream()
                .collect(Collectors.toMap(Organization::getId, Organization::getName));

        Map<Long, CreditRule> creditRules = creditRuleMapper.selectList(null).stream()
                .collect(Collectors.toMap(CreditRule::getId, r -> r));

        for (ConversionRule rule : rules) {
            rule.setOriginalOrgName(orgNames.getOrDefault(rule.getOriginalOrgId(), ""));
            rule.setConvertedOrgName(orgNames.getOrDefault(rule.getConvertedOrgId(), ""));
            if (rule.getCreditRuleId() != null) {
                CreditRule cr = creditRules.get(rule.getCreditRuleId());
                if (cr != null) {
                    rule.setCreditRuleName(cr.getEventName());
                    rule.setCreditValue(cr.getCreditValue());
                }
            }
        }

        return rules;
    }
}