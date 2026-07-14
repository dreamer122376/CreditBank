package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.ApplicationDetailDTO;
import com.creditbank.mvp.dto.FlowStepDTO;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.entity.ApplicationAuditLog;
import com.creditbank.mvp.entity.CertAuditFlow;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.entity.ExpertCert;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.ApplicationAuditLogMapper;
import com.creditbank.mvp.mapper.ApplicationMapper;
import com.creditbank.mvp.mapper.CertAuditFlowMapper;
import com.creditbank.mvp.mapper.CertStandardMapper;
import com.creditbank.mvp.mapper.ExpertCertMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 业务流程管理。
 * 审批路径按业务类型分两类：
 * - 非认证业务（PROJECT_UP/EXCHANGE）：系统管理员单级审核；
 * - 认证业务（CERT_APPLY/EXPERT_CERT）：按认证标准（cert_standard）配置的
 *   审批链（cert_audit_flow）逐节点流转，need_manual_audit=0 的标准提交即通过。
 */
@Service
public class ApplicationService {

    /** 审批状态：0草稿，1审核中，3已通过，4已驳回。2 为旧数据遗留，兼容为"审核中"。 */
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_IN_REVIEW = 1;
    public static final int STATUS_LEGACY_IN_REVIEW = 2;
    public static final int STATUS_APPROVED = 3;
    public static final int STATUS_REJECTED = 4;

    /** 认证类业务：审批链由认证流程表驱动 */
    private static final Set<String> CERT_BIZ = new HashSet<>(Arrays.asList("CERT_APPLY", "EXPERT_CERT", "ORG_REGISTER"));

    private static final Map<String, String> BIZ_TYPE_NAME = new HashMap<>();
    private static final Map<Integer, String[]> STATUS_MAP = new HashMap<>();

    static {
        BIZ_TYPE_NAME.put("PROJECT_UP", "项目上架审核");
        BIZ_TYPE_NAME.put("EXCHANGE", "学分转换申请");
        BIZ_TYPE_NAME.put("CERT_APPLY", "证书认证申请");
        BIZ_TYPE_NAME.put("EXPERT_CERT", "专家认证申请");
        BIZ_TYPE_NAME.put("UNFREEZE_APPEAL", "解冻申诉");
        BIZ_TYPE_NAME.put("ORG_REGISTER", "机构入驻申请");
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final String DEFAULT_ADMIN_PASSWORD = "123456";

    private final ApplicationMapper applicationMapper;
    private final ApplicationAuditLogMapper applicationAuditLogMapper;
    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;
    private final ExpertCertMapper expertCertMapper;
    private final CertStandardMapper certStandardMapper;
    private final CertAuditFlowMapper certAuditFlowMapper;
    private final StudentCertService studentCertService;
    private final PasswordEncoder passwordEncoder;
    private final ExpertCertService expertCertService;

    public ApplicationService(ApplicationMapper applicationMapper,
                              ApplicationAuditLogMapper applicationAuditLogMapper,
                              SysUserMapper sysUserMapper,
                              OrganizationMapper organizationMapper,
                              ExpertCertMapper expertCertMapper,
                              CertStandardMapper certStandardMapper,
                              CertAuditFlowMapper certAuditFlowMapper,
                              StudentCertService studentCertService,
                              ExpertCertService expertCertService,
                              PasswordEncoder passwordEncoder) {
        this.applicationMapper = applicationMapper;
        this.applicationAuditLogMapper = applicationAuditLogMapper;
        this.sysUserMapper = sysUserMapper;
        this.organizationMapper = organizationMapper;
        this.expertCertMapper = expertCertMapper;
        this.certStandardMapper = certStandardMapper;
        this.certAuditFlowMapper = certAuditFlowMapper;
        this.studentCertService = studentCertService;
        this.expertCertService = expertCertService;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== 查询 ====================

    /**
     * 可见性：admin 看全部；org_admin 看本机构的 + 审批链轮到自己的；
     * 专家/学生看自己发起的 + 审批链轮到自己的。
     */
    public List<ApplicationDetailDTO> list(String role, Long userId) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
                .orderByDesc(Application::getAppliedAt);

        if (!"admin".equals(role)) {
            // 审批链上分配给我的节点（无论我是什么角色）
            List<Long> myNodeIds = certAuditFlowMapper.selectList(
                            new LambdaQueryWrapper<CertAuditFlow>().eq(CertAuditFlow::getAuditorId, userId))
                    .stream().map(CertAuditFlow::getId).collect(Collectors.toList());

            if ("org_admin".equals(role)) {
                SysUser user = sysUserMapper.selectById(userId);
                if (user == null) {
                    throw new BizException("用户不存在：" + userId);
                }
                Long orgId = user.getOrgId();
                wrapper.and(w -> {
                    w.eq(Application::getOrgId, orgId);
                    if (!myNodeIds.isEmpty()) {
                        w.or().in(Application::getCurrentNodeId, myNodeIds);
                    }
                });
            } else {
                wrapper.and(w -> {
                    w.eq(Application::getApplicantId, userId);
                    if (!myNodeIds.isEmpty()) {
                        w.or().in(Application::getCurrentNodeId, myNodeIds);
                    }
                });
            }
        }

        return toDetailList(applicationMapper.selectList(wrapper), role, userId);
    }

    // ==================== 提交 ====================

    @Transactional(rollbackFor = Exception.class)
    public Application submit(Application app) {
        if (app.getBizType() == null || !BIZ_TYPE_NAME.containsKey(app.getBizType())) {
            throw new BizException("非法的业务类型：" + app.getBizType());
        }

        if ("ORG_REGISTER".equals(app.getBizType())) {
            return submitOrgRegister(app);
        }

        SysUser applicant = app.getApplicantId() == null ? null : sysUserMapper.selectById(app.getApplicantId());
        if (applicant == null) {
            throw new BizException("申请人不存在：" + app.getApplicantId());
        }
        if (app.getOrgId() == null) {
            app.setOrgId(applicant.getOrgId());
        }

        app.setId(null);
        app.setRejectReason(null);
        app.setCurrentNodeId(null);

        if (isCertBiz(app.getBizType())) {
            CertStandard standard = loadStandardForSubmit(app, applicant);
            if (standard.getNeedManualAudit() != null && standard.getNeedManualAudit() == 0) {
                // 自动通过型标准：提交即通过
                app.setCurrentStatus(STATUS_APPROVED);
                applicationMapper.insert(app);
                onApproved(app);
                return applicationMapper.selectById(app.getId());
            }
            if (standard.getFirstNodeId() == null) {
                throw new BizException("该认证标准尚未配置审批流程，请联系管理员在流程管理中配置");
            }
            app.setCurrentNodeId(standard.getFirstNodeId());
            app.setCurrentStatus(STATUS_IN_REVIEW);
        } else {
            // 非认证业务：系统管理员单级审核
            app.setCurrentStatus(STATUS_IN_REVIEW);
        }

        applicationMapper.insert(app);
        return applicationMapper.selectById(app.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Application resubmit(Long id, Long userId, String formData) {
        Application app = applicationMapper.selectById(id);
        if (app == null) {
            throw new BizException("申请不存在：" + id);
        }
        if (userId == null || !userId.equals(app.getApplicantId())) {
            throw new BizException("只能重新提交自己的申请");
        }
        int status = app.getCurrentStatus() != null ? app.getCurrentStatus() : STATUS_DRAFT;
        if (status != STATUS_REJECTED) {
            throw new BizException("只有已驳回的申请可以重新提交");
        }
        SysUser applicant = sysUserMapper.selectById(app.getApplicantId());
        if (applicant == null) {
            throw new BizException("申请人不存在：" + app.getApplicantId());
        }
        if (app.getOrgId() == null) {
            app.setOrgId(applicant.getOrgId());
        }

        if (formData != null && !formData.trim().isEmpty()) {
            app.setFormData(formData);
        }
        app.setRejectReason(null);
        app.setCurrentNodeId(null);

        if (isCertBiz(app.getBizType())) {
            CertStandard standard = loadStandardForSubmit(app, applicant);
            if (standard.getNeedManualAudit() != null && standard.getNeedManualAudit() == 0) {
                app.setCurrentStatus(STATUS_APPROVED);
            } else {
                if (standard.getFirstNodeId() == null) {
                    throw new BizException("该认证标准尚未配置审批流程，请联系管理员在流程管理中配置");
                }
                app.setCurrentNodeId(standard.getFirstNodeId());
                app.setCurrentStatus(STATUS_IN_REVIEW);
            }
        } else {
            app.setCurrentStatus(STATUS_IN_REVIEW);
        }

        LocalDateTime now = LocalDateTime.now();
        app.setAppliedAt(now);
        app.setUpdatedAt(now);
        int rows = applicationMapper.updateById(app);
        if (rows == 0) {
            throw new BizException("申请重新提交失败");
        }
        if (app.getCurrentStatus() == STATUS_APPROVED) {
            onApproved(app);
        }
        return applicationMapper.selectById(app.getId());
    }

    /** 认证业务提交校验，返回对应的认证标准 */
    private CertStandard loadStandardForSubmit(Application app, SysUser applicant) {
        Long standardId = readStandardId(app.getFormData());
        if (standardId == null) {
            throw new BizException("请选择要申请的认证标准");
        }
        CertStandard standard = certStandardMapper.selectById(standardId);
        if (standard == null || standard.getIsEnabled() == null || standard.getIsEnabled() != 1) {
            throw new BizException("认证标准不存在或已停用：" + standardId);
        }
        if (standard.getTargetRole() != null && !standard.getTargetRole().equals(applicant.getRole())) {
            throw new BizException("该认证标准的适用对象不是你的角色，无法申请");
        }
        if ("EXPERT_CERT".equals(app.getBizType())) {
            if (!"expert".equals(applicant.getRole())) {
                throw new BizException("只有专家账号可以申请领域认证");
            }
            if (expertCertService.hasCert(app.getApplicantId(), standardId)) {
                throw new BizException("你已持有该认证标准的有效评审资质，无需重复申请");
            }
            // 同一标准只允许一条在审申请；不同标准可并行申请
            List<Application> pendingApps = applicationMapper.selectList(
                    new LambdaQueryWrapper<Application>()
                            .eq(Application::getBizType, "EXPERT_CERT")
                            .eq(Application::getApplicantId, app.getApplicantId())
                            .in(Application::getCurrentStatus, STATUS_IN_REVIEW, STATUS_LEGACY_IN_REVIEW));
            boolean samePending = pendingApps.stream()
                    .filter(p -> app.getId() == null || !p.getId().equals(app.getId()))
                    .anyMatch(p -> standardId.equals(readStandardIdQuiet(p.getFormData())));
            if (samePending) {
                throw new BizException("该认证标准已有一条申请正在审核中，请等待结果");
            }
        }
        return standard;
    }

    // ==================== 机构入驻申请提交 ====================

    /** 机构入驻认证标准ID */
    private static final Long ORG_REGISTER_STANDARD_ID = 5L;

    @Transactional(rollbackFor = Exception.class)
    public Application submitOrgRegister(Application app) {
        String formData = app.getFormData() == null ? "" : app.getFormData();
        String orgName = readText(formData, "orgName");
        String applicantName = readText(formData, "applicantName");
        String contactPerson = readText(formData, "contactPerson");
        String contactPhone = readText(formData, "contactPhone");

        if (orgName == null || orgName.trim().isEmpty()) {
            throw new BizException("机构名称不能为空");
        }
        if (applicantName == null || applicantName.trim().isEmpty()) {
            throw new BizException("申请人不能为空");
        }

        app.setId(null);
        app.setApplicantId(-1L);
        app.setOrgId(null);
        app.setExpertId(null);
        app.setRejectReason(null);

        // 关联机构入驻认证标准（ID=5），设置审批流程
        app.setBizKey(ORG_REGISTER_STANDARD_ID);
        CertStandard standard = certStandardMapper.selectById(ORG_REGISTER_STANDARD_ID);
        if (standard != null && standard.getFirstNodeId() != null) {
            app.setCurrentNodeId(standard.getFirstNodeId());
        } else {
            app.setCurrentNodeId(null);
        }
        app.setCurrentStatus(STATUS_IN_REVIEW);

        applicationMapper.insert(app);
        return applicationMapper.selectById(app.getId());
    }

    // ==================== 机构入驻状态查询（公开接口） ====================

    public Map<String, Object> queryOrgRegisterStatus(String orgName, String applicantName) {
        List<Application> apps = applicationMapper.selectList(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getBizType, "ORG_REGISTER")
                        .orderByDesc(Application::getAppliedAt));

        for (Application app : apps) {
            String name = readText(app.getFormData(), "orgName");
            String applicant = readText(app.getFormData(), "applicantName");
            if (orgName.equals(name) && applicantName.equals(applicant)) {
                Map<String, Object> result = new HashMap<>();
                int status = app.getCurrentStatus() != null ? app.getCurrentStatus() : 0;
                result.put("status", status);
                if (status == STATUS_APPROVED) {
                    result.put("message", "恭喜，您的入驻申请已通过！");
                    result.put("adminUsername", "org_admin_" + app.getOrgId());
                    result.put("adminPassword", DEFAULT_ADMIN_PASSWORD);
                } else if (status == STATUS_REJECTED) {
                    result.put("message", "您的申请未通过，请联系客服。");
                    result.put("reason", app.getRejectReason());
                } else {
                    result.put("message", "您的申请正在审核中，请耐心等待。");
                }
                return result;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("status", -1);
        result.put("message", "未找到匹配的申请记录，请检查输入的机构名称和申请人。");
        return result;
    }

    // ==================== 审批 ====================

    /**
     * 审批。认证业务：只有当前节点的审核人本人（或 admin 代审）可操作，
     * 通过则流转到下一节点，末节点通过为终审；非认证业务：仅 admin 可审，单级。
     */
    @Transactional(rollbackFor = Exception.class)
    public Application audit(Long id, String role, Long userId, boolean approve, String reason) {
        Application app = applicationMapper.selectById(id);
        if (app == null) {
            throw new BizException("申请不存在：" + id);
        }
        int status = app.getCurrentStatus() != null ? app.getCurrentStatus() : STATUS_DRAFT;
        if (status != STATUS_IN_REVIEW && status != STATUS_LEGACY_IN_REVIEW) {
            throw new BizException("当前状态不可审核");
        }

        CertAuditFlow node = null;
        Long currentNodeId = app.getCurrentNodeId();
        if (isCertBiz(app.getBizType()) && currentNodeId != null) {
            node = certAuditFlowMapper.selectById(currentNodeId);
            if (node == null) {
                throw new BizException("审批流程节点已被删除，请联系管理员处理该申请");
            }
            boolean isNodeAuditor = userId != null && userId.equals(node.getAuditorId());
            if (!isNodeAuditor) {
                throw new BizException("当前环节的审核人不是你");
            }
        } else {
            // 非认证业务，以及无流程节点的旧数据：仅系统管理员可审
            if (!"admin".equals(role)) {
                throw new BizException("该申请只能由系统管理员审核");
            }
        }

        // 记录审核日志（在更新申请状态之前）
        ApplicationAuditLog auditLog = new ApplicationAuditLog();
        auditLog.setApplicationId(app.getId());
        auditLog.setNodeId(currentNodeId);
        auditLog.setStatus(approve ? 2 : 3); // 2通过/3驳回
        auditLog.setRejectReason(approve ? null : reason);
        auditLog.setCreatedAt(LocalDateTime.now());
        applicationAuditLogMapper.insert(auditLog);

        if (!approve) {
            if (reason == null || reason.trim().isEmpty()) {
                throw new BizException("驳回时必须填写原因");
            }
            app.setCurrentStatus(STATUS_REJECTED);
            app.setRejectReason(reason);
        } else if (node != null && node.getNextNodeId() != null) {
            // 链上还有下一环节：继续流转，状态保持审核中
            app.setCurrentNodeId(node.getNextNodeId());
            app.setCurrentStatus(STATUS_IN_REVIEW);
        } else {
            app.setCurrentStatus(STATUS_APPROVED);
        }

        int rows = applicationMapper.updateById(app);
        if (rows == 0) {
            throw new BizException("申请数据更新失败");
        }

        if (app.getCurrentStatus() == STATUS_APPROVED) {
            onApproved(app);
        }
        return app;
    }

    /** 终审通过后的落地动作（与状态更新同一事务） */
    private void onApproved(Application app) {
        if ("EXPERT_CERT".equals(app.getBizType())) {
            issueExpertCert(app);
        }
        if ("CERT_APPLY".equals(app.getBizType())) {
            studentCertService.issueForApplication(app);
        }
        if ("ORG_REGISTER".equals(app.getBizType())) {
            onOrgRegisterApproved(app);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void onOrgRegisterApproved(Application app) {
        String formData = app.getFormData() == null ? "" : app.getFormData();
        String orgName = readText(formData, "orgName");
        String applicantName = readText(formData, "applicantName");
        String contactPerson = readText(formData, "contactPerson");
        String contactPhone = readText(formData, "contactPhone");
        String address = readText(formData, "address");

        Organization org = new Organization();
        org.setName(orgName);
        org.setContactPerson(contactPerson != null ? contactPerson : applicantName);
        org.setContactPhone(contactPhone);
        org.setAddress(address);
        org.setStatus(OrganizationService.STATUS_ENABLED);
        organizationMapper.insert(org);

        // 自动生成管理员账号：org_admin_{机构ID}，默认密码 123456
        String adminUsername = "org_admin_" + org.getId();
        String adminPassword = DEFAULT_ADMIN_PASSWORD;

        SysUser admin = new SysUser();
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRealName(applicantName);
        admin.setPhone(contactPhone);
        admin.setRole("org_admin");
        admin.setOrgId(org.getId());
        admin.setBalance(0);
        admin.setStatus(1);
        admin.setCreatedAt(LocalDateTime.now());
        sysUserMapper.insert(admin);

        app.setOrgId(org.getId());
        applicationMapper.updateById(app);
    }

    private void issueExpertCert(Application app) {
        Long standardId = readStandardId(app.getFormData());
        if (standardId == null) {
            throw new BizException("申请数据缺少认证标准，无法发证");
        }
        if (expertCertService.hasCert(app.getApplicantId(), standardId)) {
            return; // 已持证（并发或重复审批），幂等处理
        }
        CertStandard standard = certStandardMapper.selectById(standardId);
        String fieldName = readText(app.getFormData(), "fieldName");
        if (fieldName == null || fieldName.trim().isEmpty()) {
            fieldName = standard != null ? standard.getStandardName() : ("认证标准#" + standardId);
        }

        LocalDateTime now = LocalDateTime.now();
        ExpertCert cert = new ExpertCert();
        cert.setExpertId(app.getApplicantId());
        cert.setCertStandardId(standardId);
        cert.setFieldName(fieldName);
        cert.setApplicationId(app.getId());
        cert.setStatus(1);
        cert.setIssuedAt(now);
        cert.setValidUntil(now.plusYears(ExpertCertService.VALID_YEARS));
        expertCertMapper.insert(cert);

        expertCertService.syncExpertField(app.getApplicantId());
    }

    // ==================== DTO 组装 ====================

    private List<ApplicationDetailDTO> toDetailList(List<Application> apps, String role, Long userId) {
        // 认证业务：申请单 → 认证标准
        Map<Long, Long> appStandardIds = new HashMap<>();
        for (Application app : apps) {
            if (isCertBiz(app.getBizType())) {
                Long sid = readStandardIdQuiet(app.getFormData());
                if (sid == null && app.getBizKey() != null) {
                    sid = app.getBizKey();
                }
                if (sid != null) {
                    appStandardIds.put(app.getId(), sid);
                }
            }
        }

        // 标准 → 审批链（按 first_node_id 沿 next_node_id 展开成有序列表）
        Map<Long, CertStandard> standardMap = new HashMap<>();
        Map<Long, List<CertAuditFlow>> chainMap = new HashMap<>();
        Set<Long> auditorIds = new HashSet<>();
        if (!appStandardIds.isEmpty()) {
            Set<Long> standardIds = new HashSet<>(appStandardIds.values());
            for (CertStandard s : certStandardMapper.selectBatchIds(standardIds)) {
                standardMap.put(s.getId(), s);
            }
            List<CertAuditFlow> allNodes = certAuditFlowMapper.selectList(
                    new LambdaQueryWrapper<CertAuditFlow>().in(CertAuditFlow::getCertStandardId, standardIds));
            Map<Long, CertAuditFlow> nodeById = allNodes.stream()
                    .collect(Collectors.toMap(CertAuditFlow::getId, n -> n));
            for (CertStandard s : standardMap.values()) {
                List<CertAuditFlow> chain = new ArrayList<>();
                Long cursor = s.getFirstNodeId();
                int guard = 0;
                while (cursor != null && guard++ <= allNodes.size()) {
                    CertAuditFlow n = nodeById.get(cursor);
                    if (n == null) {
                        break;
                    }
                    chain.add(n);
                    auditorIds.add(n.getAuditorId());
                    cursor = n.getNextNodeId();
                }
                chainMap.put(s.getId(), chain);
            }
        }

        // 批量取人名 / 机构名
        Set<Long> userIds = new HashSet<>(auditorIds);
        for (Application app : apps) {
            if (app.getApplicantId() != null) {
                userIds.add(app.getApplicantId());
            }
            if (app.getExpertId() != null) {
                userIds.add(app.getExpertId());
            }
        }
        Map<Long, String> userNames = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (SysUser u : sysUserMapper.selectBatchIds(userIds)) {
                userNames.put(u.getId(), u.getRealName());
            }
        }
        Map<Long, String> orgNames = new HashMap<>();
        Set<Long> orgIds = apps.stream()
                .map(Application::getOrgId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (!orgIds.isEmpty()) {
            for (Organization o : organizationMapper.selectBatchIds(orgIds)) {
                orgNames.put(o.getId(), o.getName());
            }
        }

        List<ApplicationDetailDTO> result = new ArrayList<>();
        for (Application app : apps) {
            ApplicationDetailDTO dto = new ApplicationDetailDTO();
            dto.setId(app.getId());
            dto.setBizType(app.getBizType());
            dto.setBizTypeName(BIZ_TYPE_NAME.getOrDefault(app.getBizType(), app.getBizType()));
            dto.setApplicantId(app.getApplicantId());
            dto.setApplicantName(userNames.getOrDefault(app.getApplicantId(), "未知"));
            dto.setOrgId(app.getOrgId());
            dto.setOrgName(orgNames.getOrDefault(app.getOrgId(), ""));
            // ORG_REGISTER 为公开提交，从 formData 读取申请人名和机构名
            if ("ORG_REGISTER".equals(app.getBizType())) {
                if ("未知".equals(dto.getApplicantName())) {
                    dto.setApplicantName(readText(app.getFormData(), "applicantName"));
                }
                if (dto.getOrgName() == null || dto.getOrgName().isEmpty()) {
                    dto.setOrgName(readText(app.getFormData(), "orgName"));
                }
            }
            dto.setExpertId(app.getExpertId());
            dto.setExpertName(userNames.getOrDefault(app.getExpertId(), ""));
            dto.setCurrentStatus(app.getCurrentStatus());
            dto.setRejectReason(app.getRejectReason());
            dto.setFormData(app.getFormData());
            dto.setAppliedAt(app.getAppliedAt());
            dto.setUpdatedAt(app.getUpdatedAt());
            dto.setCurrentNodeId(app.getCurrentNodeId());

            int status = app.getCurrentStatus() != null ? app.getCurrentStatus() : STATUS_DRAFT;
            boolean inReview = status == STATUS_IN_REVIEW || status == STATUS_LEGACY_IN_REVIEW;

            // 审批链展示 + 当前审核人
            Long standardId = appStandardIds.get(app.getId());
            List<CertAuditFlow> chain = standardId != null ? chainMap.get(standardId) : null;
            if (chain != null && !chain.isEmpty()) {
                List<FlowStepDTO> steps = new ArrayList<>();
                boolean beforeCurrent = app.getCurrentNodeId() != null;
                for (int i = 0; i < chain.size(); i++) {
                    CertAuditFlow n = chain.get(i);
                    FlowStepDTO step = new FlowStepDTO();
                    step.setStepNo(i + 1);
                    step.setNodeId(n.getId());
                    step.setAuditorName(userNames.getOrDefault(n.getAuditorId(), "用户#" + n.getAuditorId()));
                    boolean isCurrent = n.getId().equals(app.getCurrentNodeId());
                    if (status == STATUS_APPROVED) {
                        step.setState("done");
                    } else if (isCurrent) {
                        step.setState(status == STATUS_REJECTED ? "rejected" : "current");
                        beforeCurrent = false;
                    } else {
                        step.setState(beforeCurrent ? "done" : "pending");
                    }
                    steps.add(step);
                    if (isCurrent && inReview) {
                        dto.setCurrentAuditorName(step.getAuditorName());
                    }
                }
                dto.setFlowSteps(steps);
            }

            // 状态名
            if (inReview) {
                if (dto.getCurrentAuditorName() != null) {
                    dto.setStatusName("待" + dto.getCurrentAuditorName() + "审核");
                } else if (!isCertBiz(app.getBizType())) {
                    dto.setStatusName("待管理员审核");
                } else {
                    dto.setStatusName("审核中");
                }
                dto.setStatusType("warning");
            } else if (status == STATUS_APPROVED) {
                dto.setStatusName("已通过");
                dto.setStatusType("success");
            } else if (status == STATUS_REJECTED) {
                dto.setStatusName("已驳回");
                dto.setStatusType("danger");
            } else {
                dto.setStatusName("草稿");
                dto.setStatusType("info");
            }

            // 当前登录人能否审批
            boolean canAudit = false;
            if (inReview && role != null) {
                if ("admin".equals(role)) {
                    canAudit = true;
                } else if (isCertBiz(app.getBizType()) && app.getCurrentNodeId() != null && chain != null) {
                    canAudit = chain.stream().anyMatch(n ->
                            n.getId().equals(app.getCurrentNodeId())
                                    && n.getAuditorId() != null
                                    && n.getAuditorId().equals(userId));
                }
            }
            dto.setCanAudit(canAudit);

            result.add(dto);
        }
        return result;
    }

    // ==================== 工具 ====================

    private boolean isCertBiz(String bizType) {
        return CERT_BIZ.contains(bizType);
    }

    /** 兼容两种键名：EXPERT_CERT 用 certStandardId，CERT_APPLY 历史数据用 standardId */
    private Long readStandardId(String formData) {
        Long id = readLong(formData, "certStandardId");
        return id != null ? id : readLong(formData, "standardId");
    }

    private Long readStandardIdQuiet(String formData) {
        try {
            return readStandardId(formData);
        } catch (Exception e) {
            return null;
        }
    }

    /** 从 form_data JSON 里读一个 long 字段，读不到返回 null */
    private Long readLong(String formData, String field) {
        if (formData == null || formData.trim().isEmpty()) {
            return null;
        }
        try {
            JsonNode node = JSON.readTree(formData);
            return node.hasNonNull(field) ? node.get(field).asLong() : null;
        } catch (Exception e) {
            throw new BizException("申请表单数据不是合法的 JSON");
        }
    }

    private String readText(String formData, String field) {
        if (formData == null || formData.trim().isEmpty()) {
            return null;
        }
        try {
            JsonNode node = JSON.readTree(formData);
            return node.hasNonNull(field) ? node.get(field).asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 审核日志查询 ====================

    /**
     * 获取申请单的审核日志列表
     */
    public List<ApplicationAuditLog> getAuditLogs(Long applicationId) {
        return applicationAuditLogMapper.selectList(
                new LambdaQueryWrapper<ApplicationAuditLog>()
                        .eq(ApplicationAuditLog::getApplicationId, applicationId)
                        .orderByAsc(ApplicationAuditLog::getCreatedAt));
    }
}
