package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.ConversionApplication;
import com.creditbank.mvp.entity.ConversionRule;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.ConversionApplicationMapper;
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
public class ConversionApplicationService {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    public static final String APPLY_TYPE_RULE_CONVERT = "RULE_CONVERT";
    public static final String APPLY_TYPE_RULE_ADD = "RULE_ADD";

    private final ConversionApplicationMapper applicationMapper;
    private final ConversionRuleMapper conversionRuleMapper;
    private final CreditRuleMapper creditRuleMapper;
    private final OrganizationMapper organizationMapper;
    private final SysUserMapper sysUserMapper;
    private final PointService pointService;

    public ConversionApplicationService(ConversionApplicationMapper applicationMapper,
                                        ConversionRuleMapper conversionRuleMapper,
                                        CreditRuleMapper creditRuleMapper,
                                        OrganizationMapper organizationMapper,
                                        SysUserMapper sysUserMapper,
                                        PointService pointService) {
        this.applicationMapper = applicationMapper;
        this.conversionRuleMapper = conversionRuleMapper;
        this.creditRuleMapper = creditRuleMapper;
        this.organizationMapper = organizationMapper;
        this.sysUserMapper = sysUserMapper;
        this.pointService = pointService;
    }

    public List<ConversionApplication> list() {
        return enrichWithRelatedData(applicationMapper.selectList(
                new LambdaQueryWrapper<ConversionApplication>().orderByDesc(ConversionApplication::getId)));
    }

    public List<ConversionApplication> listByStudentId(Long studentId) {
        return enrichWithRelatedData(applicationMapper.selectList(
                new LambdaQueryWrapper<ConversionApplication>()
                        .eq(ConversionApplication::getStudentId, studentId)
                        .orderByDesc(ConversionApplication::getId)));
    }

    public List<ConversionApplication> listByStatus(Integer status) {
        return enrichWithRelatedData(applicationMapper.selectList(
                new LambdaQueryWrapper<ConversionApplication>()
                        .eq(ConversionApplication::getStatus, status)
                        .orderByDesc(ConversionApplication::getId)));
    }

    public ConversionApplication getById(Long id) {
        ConversionApplication application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BizException("转换申请不存在：" + id);
        }
        return enrichWithRelatedData(List.of(application)).get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    public ConversionApplication submit(ConversionApplication application) {
        if (application.getStudentId() == null) {
            throw new BizException("学生ID不能为空");
        }
        SysUser student = sysUserMapper.selectById(application.getStudentId());
        if (student == null) {
            throw new BizException("学生不存在：" + application.getStudentId());
        }
        if (!"student".equals(student.getRole())) {
            throw new BizException("只有学生可以提交转换申请");
        }

        if (application.getOriginalName() == null || application.getOriginalName().trim().isEmpty()) {
            throw new BizException("原成果名称不能为空");
        }
        if (application.getConvertedName() == null || application.getConvertedName().trim().isEmpty()) {
            throw new BizException("转换后成果名称不能为空");
        }
        if (application.getOriginalType() == null || application.getOriginalType().trim().isEmpty()) {
            throw new BizException("原成果类型不能为空");
        }
        if (application.getConvertedType() == null || application.getConvertedType().trim().isEmpty()) {
            throw new BizException("转换后成果类型不能为空");
        }
        if (application.getApplyType() == null || application.getApplyType().trim().isEmpty()) {
            throw new BizException("申请类型不能为空");
        }

        if (application.getRuleId() != null) {
            ConversionRule rule = conversionRuleMapper.selectById(application.getRuleId());
            if (rule == null) {
                throw new BizException("转换规则不存在：" + application.getRuleId());
            }
            if (rule.getIsEnabled() != null && rule.getIsEnabled() != 1) {
                throw new BizException("转换规则已停用");
            }
            if (rule.getEffectiveStart() != null && LocalDateTime.now().isBefore(rule.getEffectiveStart())) {
                throw new BizException("转换规则尚未生效");
            }
            if (rule.getEffectiveEnd() != null && LocalDateTime.now().isAfter(rule.getEffectiveEnd())) {
                throw new BizException("转换规则已过期");
            }
        }

        application.setId(null);
        application.setStatus(STATUS_PENDING);
        application.setRejectReason(null);
        application.setApprovedAt(null);
        application.setCreatedAt(LocalDateTime.now());
        applicationMapper.insert(application);
        return getById(application.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ConversionApplication audit(Long id, boolean approve, String reason, Long operatorId) {
        ConversionApplication application = getById(id);
        int status = application.getStatus() != null ? application.getStatus() : STATUS_PENDING;

        if (status != STATUS_PENDING) {
            throw new BizException("当前状态不可审核");
        }

        if (!approve) {
            if (reason == null || reason.trim().isEmpty()) {
                throw new BizException("驳回时必须填写原因");
            }
            application.setStatus(STATUS_REJECTED);
            application.setRejectReason(reason);
        } else {
            application.setStatus(STATUS_APPROVED);
            application.setApprovedAt(LocalDateTime.now());
            if (application.getApplyType().equals(APPLY_TYPE_RULE_CONVERT)) {
                grantCredit(application);
            }
        }

        applicationMapper.updateById(application);
        return getById(id);
    }

    private void grantCredit(ConversionApplication application) {
        Long creditRuleId = null;
        String eventCode = null;

        if (application.getRuleId() != null) {
            ConversionRule rule = conversionRuleMapper.selectById(application.getRuleId());
            if (rule != null && rule.getCreditRuleId() != null) {
                creditRuleId = rule.getCreditRuleId();
            }
        }

        if (creditRuleId != null) {
            CreditRule creditRule = creditRuleMapper.selectById(creditRuleId);
            if (creditRule != null) {
                eventCode = creditRule.getEventCode();
            }
        }

        if (eventCode == null) {
            eventCode = application.getConvertedType();
        }

        pointService.earn(application.getStudentId(), eventCode, null);
    }

    private List<ConversionApplication> enrichWithRelatedData(List<ConversionApplication> applications) {
        if (applications.isEmpty()) {
            return applications;
        }

        Map<Long, String> userNames = sysUserMapper.selectList(null).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));

        Map<Long, String> orgNames = organizationMapper.selectList(null).stream()
                .collect(Collectors.toMap(Organization::getId, Organization::getName));

        Map<Long, ConversionRule> rules = conversionRuleMapper.selectList(null).stream()
                .collect(Collectors.toMap(ConversionRule::getId, r -> r));

        for (ConversionApplication app : applications) {
            app.setStudentName(userNames.getOrDefault(app.getStudentId(), ""));
            app.setOriginalOrgName(orgNames.getOrDefault(app.getOriginalOrgId(), ""));
            app.setConvertedOrgName(orgNames.getOrDefault(app.getConvertedOrgId(), ""));
            if (app.getRuleId() != null) {
                ConversionRule rule = rules.get(app.getRuleId());
                if (rule != null) {
                    app.setRuleName(rule.getOriginalName() + " → " + rule.getConvertedName());
                }
            }
        }

        return applications;
    }
}