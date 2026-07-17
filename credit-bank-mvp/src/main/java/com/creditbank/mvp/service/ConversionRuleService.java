package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.ConversionRule;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.ConversionRuleMapper;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
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
    private final UserOpLogMapper userOpLogMapper;

    public ConversionRuleService(ConversionRuleMapper conversionRuleMapper,
                                 OrganizationMapper organizationMapper,
                                 CreditRuleMapper creditRuleMapper,
                                 SysUserMapper sysUserMapper,
                                 UserOpLogMapper userOpLogMapper) {
        this.conversionRuleMapper = conversionRuleMapper;
        this.organizationMapper = organizationMapper;
        this.creditRuleMapper = creditRuleMapper;
        this.sysUserMapper = sysUserMapper;
        this.userOpLogMapper = userOpLogMapper;
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
    public ConversionRule create(ConversionRule rule, SysUser operator) {
        if (rule.getOriginalName() == null || rule.getOriginalName().trim().isEmpty()) {
            throw new BizException("原成果名称不能为空");
        }
        if (rule.getConvertedName() == null || rule.getConvertedName().trim().isEmpty()) {
            throw new BizException("转换后成果名称不能为空");
        }
        if (rule.getOriginalType() == null || rule.getOriginalType().trim().isEmpty()) {
            throw new BizException("原成果类型不能为空");
        }
        // 方案A：creditRuleId 必填，保证转换通过后加积分能精确命中积分规则
        if (rule.getCreditRuleId() == null) {
            throw new BizException("必须关联一条积分规则（审核通过后按该规则自动加积分）");
        }
        CreditRule creditRule = creditRuleMapper.selectById(rule.getCreditRuleId());
        if (creditRule == null) {
            throw new BizException("积分规则不存在：" + rule.getCreditRuleId());
        }
        if (creditRule.getIsEnabled() == null || creditRule.getIsEnabled() != 1) {
            throw new BizException("关联的积分规则已停用，请先启用再关联");
        }
        // 自动回写 convertedType = eventCode（兜底展示），前端展示以 creditRule.eventName 为主
        if (creditRule.getEventCode() != null && !creditRule.getEventCode().trim().isEmpty()) {
            rule.setConvertedType(creditRule.getEventCode());
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
        ConversionRule saved = getById(rule.getId());
        // 写操作日志：目标用户为空，因为规则是配置而非针对某个用户
        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_CONVERSION_RULE, UserOpLog.ACTION_CREATE,
                "创建转换规则：" + buildRuleSummary(saved)));
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public ConversionRule update(ConversionRule rule, SysUser operator) {
        ConversionRule exist = getById(rule.getId());
        // 用户显式传入了新的 creditRuleId（非空）：重新校验并回写 convertedType
        if (rule.getCreditRuleId() != null) {
            CreditRule creditRule = creditRuleMapper.selectById(rule.getCreditRuleId());
            if (creditRule == null) {
                throw new BizException("积分规则不存在：" + rule.getCreditRuleId());
            }
            if (creditRule.getIsEnabled() == null || creditRule.getIsEnabled() != 1) {
                throw new BizException("关联的积分规则已停用，请先启用再关联");
            }
            if (creditRule.getEventCode() != null && !creditRule.getEventCode().trim().isEmpty()) {
                rule.setConvertedType(creditRule.getEventCode());
            }
        } else {
            // 没传 creditRuleId：沿用已有关联，避免误清空
            rule.setCreditRuleId(exist.getCreditRuleId());
            rule.setConvertedType(exist.getConvertedType());
        }
        if (rule.getEffectiveStart() != null && rule.getEffectiveEnd() != null) {
            if (rule.getEffectiveStart().isAfter(rule.getEffectiveEnd())) {
                throw new BizException("开始时间不能晚于结束时间");
            }
        }
        rule.setCreatedAt(exist.getCreatedAt());
        rule.setCreatedBy(exist.getCreatedBy());
        conversionRuleMapper.updateById(rule);
        ConversionRule updated = getById(rule.getId());
        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_CONVERSION_RULE, UserOpLog.ACTION_UPDATE,
                "编辑转换规则：" + buildRuleSummary(updated)));
        return updated;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, SysUser operator) {
        ConversionRule exist = getById(id);
        conversionRuleMapper.deleteById(id);
        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_CONVERSION_RULE, UserOpLog.ACTION_DELETE,
                "删除转换规则：" + buildRuleSummary(exist)));
    }

    @Transactional(rollbackFor = Exception.class)
    public ConversionRule toggleEnabled(Long id, Integer isEnabled, SysUser operator) {
        ConversionRule exist = getById(id);
        if (isEnabled == null || (isEnabled != STATUS_ENABLED && isEnabled != STATUS_DISABLED)) {
            throw new BizException("非法的状态值：" + isEnabled);
        }
        exist.setIsEnabled(isEnabled);
        conversionRuleMapper.updateById(exist);
        String actionLabel = isEnabled == STATUS_ENABLED ? "启用" : "停用";
        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_CONVERSION_RULE, UserOpLog.ACTION_CONVERSION_TOGGLE,
                actionLabel + "转换规则：" + buildRuleSummary(exist)));
        return exist;
    }

    // ========== 内部辅助 ==========

    // 构造规则的简要描述，用于写操作日志的详情字段
    private String buildRuleSummary(ConversionRule rule) {
        if (rule == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(rule.getId());
        if (rule.getOriginalName() != null) {
            sb.append(" [").append(rule.getOriginalType() == null ? "" : rule.getOriginalType())
              .append("] ").append(rule.getOriginalName());
            if (rule.getOriginalOrgName() != null && !rule.getOriginalOrgName().isEmpty()) {
                sb.append("（").append(rule.getOriginalOrgName()).append("）");
            }
        }
        sb.append(" → ");
        if (rule.getConvertedName() != null) {
            sb.append("[").append(rule.getConvertedType() == null ? "" : rule.getConvertedType())
              .append("] ").append(rule.getConvertedName());
            if (rule.getConvertedOrgName() != null && !rule.getConvertedOrgName().isEmpty()) {
                sb.append("（").append(rule.getConvertedOrgName()).append("）");
            }
        }
        if (rule.getCreditValue() != null) {
            sb.append(" · 赋分").append(rule.getCreditValue());
        }
        if (rule.getIsEnabled() != null) {
            sb.append(" · ").append(rule.getIsEnabled() == STATUS_ENABLED ? "启用中" : "已停用");
        }
        return sb.toString();
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