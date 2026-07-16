package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.ConversionApplication;
import com.creditbank.mvp.entity.ConversionRule;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.ConversionApplicationMapper;
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
    private final NotificationService notificationService;
    private final UserOpLogMapper userOpLogMapper;

    public ConversionApplicationService(ConversionApplicationMapper applicationMapper,
                                        ConversionRuleMapper conversionRuleMapper,
                                        CreditRuleMapper creditRuleMapper,
                                        OrganizationMapper organizationMapper,
                                        SysUserMapper sysUserMapper,
                                        PointService pointService,
                                        NotificationService notificationService,
                                        UserOpLogMapper userOpLogMapper) {
        this.applicationMapper = applicationMapper;
        this.conversionRuleMapper = conversionRuleMapper;
        this.creditRuleMapper = creditRuleMapper;
        this.organizationMapper = organizationMapper;
        this.sysUserMapper = sysUserMapper;
        this.pointService = pointService;
        this.notificationService = notificationService;
        this.userOpLogMapper = userOpLogMapper;
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

    // org_admin 按所属机构过滤转换申请（converted_org_id 匹配本机构，或申请本身无机构限定）
    public List<ConversionApplication> listByOrgId(Long orgId, Integer status) {
        LambdaQueryWrapper<ConversionApplication> wrapper = new LambdaQueryWrapper<ConversionApplication>()
                .and(w -> w.eq(ConversionApplication::getConvertedOrgId, orgId)
                        .or().isNull(ConversionApplication::getConvertedOrgId))
                .orderByDesc(ConversionApplication::getId);
        if (status != null) {
            wrapper.eq(ConversionApplication::getStatus, status);
        }
        return enrichWithRelatedData(applicationMapper.selectList(wrapper));
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
        ConversionApplication saved = getById(application.getId());

        // 操作日志：提交人即为学生本人
        userOpLogMapper.insert(UserOpLog.createLog(
                student.getId(), student.getRealName(),
                student.getId(), student.getRealName(),
                UserOpLog.MODULE_CONVERSION_APPLY, UserOpLog.ACTION_SUBMIT,
                "提交转换申请：" + buildApplySummary(saved)));

        // 通知管理员/机构管理员有新的待审核转换申请
        notifyAdminsOfPendingApplication(saved);
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public ConversionApplication audit(Long id, boolean approve, String reason, SysUser operator) {
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
                grantCredit(application, operator.getId());
            }
        }

        applicationMapper.updateById(application);
        ConversionApplication result = getById(id);

        // 操作日志：审核维度，target=学生本人
        SysUser student = sysUserMapper.selectById(application.getStudentId());
        String action = approve ? UserOpLog.ACTION_APPROVE : UserOpLog.ACTION_REJECT;
        String detail = (approve ? "审核通过转换申请：" : "审核驳回转换申请：") + buildApplySummary(result);
        if (!approve && reason != null && !reason.trim().isEmpty()) {
            detail = detail + " · 驳回原因：" + reason.trim();
        }
        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(),
                student == null ? null : student.getId(),
                student == null ? null : student.getRealName(),
                UserOpLog.MODULE_CONVERSION_APPLY, action, detail));

        // 通知学生审核结果
        notifyStudentOfAuditResult(result, approve, reason);
        return result;
    }

    // 操作日志详情描述：简化阅读
    private String buildApplySummary(ConversionApplication app) {
        if (app == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(app.getId());
        if (app.getApplyType() != null) {
            sb.append(" [").append(APPLY_TYPE_RULE_CONVERT.equals(app.getApplyType()) ? "规则转换" : "自定义转换").append("]");
        }
        if (app.getOriginalName() != null) {
            sb.append(" [").append(app.getOriginalType() == null ? "" : app.getOriginalType())
              .append("] ").append(app.getOriginalName());
        }
        sb.append(" → ");
        if (app.getConvertedName() != null) {
            sb.append("[").append(app.getConvertedType() == null ? "" : app.getConvertedType())
              .append("] ").append(app.getConvertedName());
        }
        if (app.getConvertedOrgName() != null && !app.getConvertedOrgName().isEmpty()) {
            sb.append("（").append(app.getConvertedOrgName()).append("）");
        }
        String statusLabel = STATUS_PENDING == (app.getStatus() == null ? STATUS_PENDING : app.getStatus()) ? "待审核"
                : (STATUS_APPROVED == (app.getStatus() == null ? -1 : app.getStatus()) ? "已通过" : "已驳回");
        sb.append(" · ").append(statusLabel);
        return sb.toString();
    }

    private void grantCredit(ConversionApplication application, Long operatorId) {
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

        // 传入真实审核人 operatorId，避免操作日志误记为学生本人
        pointService.earn(application.getStudentId(), eventCode, operatorId);
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

    // 提交申请后，通知相关管理员有待审核的转换申请（跳转到管理员的审核管理页面）
    private void notifyAdminsOfPendingApplication(ConversionApplication application) {
        String title = "新的成果转换申请待审核";
        String studentName = sysUserMapper.selectById(application.getStudentId()).getRealName();
        String content = "学生「" + (studentName != null ? studentName : "ID:" + application.getStudentId())
                + "」提交了成果转换申请：" + application.getOriginalName() + " → " + application.getConvertedName()
                + "，请及时审核。";
        String dedupeKey = "CONVERSION_APPLY_PENDING:" + application.getId();
        // 管理员端统一跳转 /applications 的转换申请 tab 并筛选待审核
        String adminActionPath = "/applications?tab=conversion&filter=pending";

        // 按机构定向通知，或全平台通知管理员
        if (application.getConvertedOrgId() != null) {
            Long orgAdminId = findOrgAdminId(application.getConvertedOrgId());
            if (orgAdminId != null) {
                notificationService.sendToUser(
                        "CONVERSION_APPLY_PENDING", NotificationService.CATEGORY_CONVERSION, "INFO",
                        title, content, adminActionPath,
                        "CONVERSION_APPLICATION", application.getId(), dedupeKey,
                        orgAdminId, application.getStudentId());
            }
            // 同时通知平台 admin
            notificationService.sendToRole(
                    "CONVERSION_APPLY_PENDING", NotificationService.CATEGORY_CONVERSION, "INFO",
                    title, content, adminActionPath,
                    "CONVERSION_APPLICATION", application.getId(), dedupeKey + ":ADMIN",
                    "admin", application.getStudentId());
        } else {
            // 通用申请：通知平台 admin + 所有 org_admin
            notificationService.sendToRole(
                    "CONVERSION_APPLY_PENDING", NotificationService.CATEGORY_CONVERSION, "INFO",
                    title, content, adminActionPath,
                    "CONVERSION_APPLICATION", application.getId(), dedupeKey,
                    "admin", application.getStudentId());
            notificationService.sendToRole(
                    "CONVERSION_APPLY_PENDING", NotificationService.CATEGORY_CONVERSION, "INFO",
                    title, content, adminActionPath,
                    "CONVERSION_APPLICATION", application.getId(), dedupeKey + ":ORG_ADMIN",
                    "org_admin", application.getStudentId());
        }
    }

    // 审核完成后通知学生审核结果（携带申请 id 与结果状态便于跳转锚定）
    private void notifyStudentOfAuditResult(ConversionApplication application, boolean approve, String reason) {
        String eventCode = approve ? "CONVERSION_APPLY_APPROVED" : "CONVERSION_APPLY_REJECTED";
        String level = approve ? "SUCCESS" : "WARNING";
        String title = approve ? "成果转换申请已通过" : "成果转换申请已驳回";
        StringBuilder content = new StringBuilder();
        content.append("您的成果转换申请：").append(application.getOriginalName())
                .append(" → ").append(application.getConvertedName());
        if (approve) {
            content.append(" 已审核通过。");
            if (APPLY_TYPE_RULE_CONVERT.equals(application.getApplyType())) {
                content.append(" 对应积分已自动发放至您的账户。");
            }
        } else {
            content.append(" 已被驳回。");
            if (reason != null && !reason.trim().isEmpty()) {
                content.append(" 驳回原因：").append(reason);
            }
        }
        String dedupeKey = eventCode + ":" + application.getId();
        // 携带本次申请 id 和审核结果，学生端跳转后可快速定位
        String studentActionPath = "/conversion-apply?id=" + application.getId()
                + "&status=" + (approve ? "APPROVED" : "REJECTED");
        notificationService.sendToUser(
                eventCode, NotificationService.CATEGORY_CONVERSION, level,
                title, content.toString(), studentActionPath,
                "CONVERSION_APPLICATION", application.getId(), dedupeKey,
                application.getStudentId(), application.getStudentId());
    }

    // 根据机构ID查找机构管理员用户ID
    private Long findOrgAdminId(Long orgId) {
        if (orgId == null) {
            return null;
        }
        SysUser orgAdmin = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getOrgId, orgId)
                        .eq(SysUser::getRole, "org_admin")
                        .last("LIMIT 1"));
        return orgAdmin == null ? null : orgAdmin.getId();
    }
}