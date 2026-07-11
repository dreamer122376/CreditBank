package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.ApplicationDetailDTO;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.entity.ExpertCert;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.ApplicationMapper;
import com.creditbank.mvp.mapper.CertStandardMapper;
import com.creditbank.mvp.mapper.ExpertCertMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    /** 审批状态：0草稿，1待机构审核，2待专家评审，3已通过，4已驳回（与 StatsService 保持一致） */
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_ORG_AUDIT = 1;
    public static final int STATUS_EXPERT_AUDIT = 2;
    public static final int STATUS_APPROVED = 3;
    public static final int STATUS_REJECTED = 4;

    private static final Map<String, String> BIZ_TYPE_NAME = new HashMap<>();
    private static final Map<Integer, String[]> STATUS_MAP = new HashMap<>();

    static {
        BIZ_TYPE_NAME.put("PROJECT_UP", "项目上架审核");
        BIZ_TYPE_NAME.put("EXCHANGE", "学分转换申请");
        BIZ_TYPE_NAME.put("CERT_APPLY", "证书认证申请");
        BIZ_TYPE_NAME.put("EXPERT_CERT", "专家认证申请");

        STATUS_MAP.put(STATUS_DRAFT, new String[]{"草稿", "info"});
        STATUS_MAP.put(STATUS_ORG_AUDIT, new String[]{"待机构审核", "warning"});
        STATUS_MAP.put(STATUS_EXPERT_AUDIT, new String[]{"待专家评审", "warning"});
        STATUS_MAP.put(STATUS_APPROVED, new String[]{"已通过", "success"});
        STATUS_MAP.put(STATUS_REJECTED, new String[]{"已驳回", "danger"});
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    private final ApplicationMapper applicationMapper;
    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;
    private final ExpertCertMapper expertCertMapper;
    private final CertStandardMapper certStandardMapper;

    public ApplicationService(ApplicationMapper applicationMapper,
                              SysUserMapper sysUserMapper,
                              OrganizationMapper organizationMapper,
                              ExpertCertMapper expertCertMapper,
                              CertStandardMapper certStandardMapper) {
        this.applicationMapper = applicationMapper;
        this.sysUserMapper = sysUserMapper;
        this.organizationMapper = organizationMapper;
        this.expertCertMapper = expertCertMapper;
        this.certStandardMapper = certStandardMapper;
    }

    /** 按角色过滤：admin 看全部，org_admin 看本机构，expert 看指派给自己的，其他人看自己发起的 */
    public List<ApplicationDetailDTO> list(String role, Long userId) {
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<Application>()
                .orderByDesc(Application::getAppliedAt);

        if ("org_admin".equals(role)) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) {
                throw new BizException("用户不存在：" + userId);
            }
            wrapper.eq(Application::getOrgId, user.getOrgId());
        } else if ("expert".equals(role)) {
            wrapper.eq(Application::getExpertId, userId);
        } else if (!"admin".equals(role)) {
            wrapper.eq(Application::getApplicantId, userId);
        }

        return toDetailList(applicationMapper.selectList(wrapper));
    }

    /**
     * 提交申请。常规类型进入"待机构审核"；
     * EXPERT_CERT（专家认证）因专家不挂机构，跳过机构审直接进入终审环节。
     */
    public Application submit(Application app) {
        if (app.getBizType() == null || !BIZ_TYPE_NAME.containsKey(app.getBizType())) {
            throw new BizException("非法的业务类型：" + app.getBizType());
        }
        if (app.getApplicantId() == null || sysUserMapper.selectById(app.getApplicantId()) == null) {
            throw new BizException("申请人不存在：" + app.getApplicantId());
        }

        if ("EXPERT_CERT".equals(app.getBizType())) {
            validateExpertCertSubmit(app);
        }
        // 学生的证书认证申请如果指定了评审专家，专家必须持有对应标准的评审资质
        if ("CERT_APPLY".equals(app.getBizType()) && app.getExpertId() != null) {
            Long standardId = readLong(app.getFormData(), "standardId");
            if (standardId != null && !hasValidCert(app.getExpertId(), standardId)) {
                throw new BizException("该专家不具备此认证标准的评审资质，请更换专家");
            }
        }

        app.setId(null);
        app.setCurrentStatus("EXPERT_CERT".equals(app.getBizType())
                ? STATUS_EXPERT_AUDIT : STATUS_ORG_AUDIT);
        app.setRejectReason(null);
        applicationMapper.insert(app);
        return applicationMapper.selectById(app.getId());
    }

    private void validateExpertCertSubmit(Application app) {
        SysUser applicant = sysUserMapper.selectById(app.getApplicantId());
        if (!"expert".equals(applicant.getRole())) {
            throw new BizException("只有专家账号可以申请领域认证");
        }
        Long standardId = readLong(app.getFormData(), "certStandardId");
        if (standardId == null) {
            throw new BizException("请选择要申请的认证标准");
        }
        CertStandard standard = certStandardMapper.selectById(standardId);
        if (standard == null || standard.getIsEnabled() == null || standard.getIsEnabled() != 1) {
            throw new BizException("认证标准不存在或已停用：" + standardId);
        }
        if (hasValidCert(app.getApplicantId(), standardId)) {
            throw new BizException("你已持有该认证标准的评审资质，无需重复申请");
        }
        Long pending = applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>()
                        .eq(Application::getBizType, "EXPERT_CERT")
                        .eq(Application::getApplicantId, app.getApplicantId())
                        .lt(Application::getCurrentStatus, STATUS_APPROVED));
        if (pending > 0) {
            throw new BizException("你有一条认证申请正在审核中，请等待结果");
        }
    }

    private boolean hasValidCert(Long expertId, Long certStandardId) {
        return expertCertMapper.selectCount(
                new LambdaQueryWrapper<ExpertCert>()
                        .eq(ExpertCert::getExpertId, expertId)
                        .eq(ExpertCert::getCertStandardId, certStandardId)
                        .eq(ExpertCert::getStatus, 1)) > 0;
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

    /**
     * 审核：机构在"待机构审核"时操作（通过→待专家评审），专家在"待专家评审"时操作（通过→已通过）。
     * admin 可以代替当前环节操作。驳回一律进入"已驳回"并记录原因。
     */
    @Transactional(rollbackFor = Exception.class)
    public Application audit(Long id, String role, boolean approve, String reason) {
        Application app = applicationMapper.selectById(id);
        if (app == null) {
            throw new BizException("申请不存在：" + id);
        }

        int status = app.getCurrentStatus() != null ? app.getCurrentStatus() : STATUS_DRAFT;
        boolean orgStage = status == STATUS_ORG_AUDIT;
        boolean expertStage = status == STATUS_EXPERT_AUDIT;

        if (!orgStage && !expertStage) {
            throw new BizException("当前状态不可审核：" + statusName(status));
        }
        boolean isExpertCert = "EXPERT_CERT".equals(app.getBizType());
        if (isExpertCert && !"admin".equals(role)) {
            throw new BizException("专家认证申请只能由系统管理员审核");
        }
        if (orgStage && !("org_admin".equals(role) || "admin".equals(role))) {
            throw new BizException("待机构审核的申请只能由机构管理员处理");
        }
        if (expertStage && !isExpertCert && !("expert".equals(role) || "admin".equals(role))) {
            throw new BizException("待专家评审的申请只能由专家处理");
        }

        if (!approve) {
            if (reason == null || reason.trim().isEmpty()) {
                throw new BizException("驳回时必须填写原因");
            }
            app.setCurrentStatus(STATUS_REJECTED);
            app.setRejectReason(reason);
        } else {
            app.setCurrentStatus(orgStage ? STATUS_EXPERT_AUDIT : STATUS_APPROVED);
        }

        int rows = applicationMapper.updateById(app);
        if (rows == 0) {
            throw new BizException("申请数据更新失败");
        }

        // 专家认证审批通过 → 发证（写认证记录 + 同步专家领域），与状态更新同一事务
        if (isExpertCert && app.getCurrentStatus() == STATUS_APPROVED) {
            issueExpertCert(app);
        }
        return app;
    }

    private void issueExpertCert(Application app) {
        Long standardId = readLong(app.getFormData(), "certStandardId");
        if (standardId == null) {
            throw new BizException("申请数据缺少认证标准，无法发证");
        }
        if (hasValidCert(app.getApplicantId(), standardId)) {
            return; // 已持证（并发或重复审批），幂等处理
        }
        CertStandard standard = certStandardMapper.selectById(standardId);
        String fieldName = readText(app.getFormData(), "fieldName");
        if (fieldName == null || fieldName.trim().isEmpty()) {
            fieldName = standard != null ? standard.getStandardName() : ("认证标准#" + standardId);
        }

        ExpertCert cert = new ExpertCert();
        cert.setExpertId(app.getApplicantId());
        cert.setCertStandardId(standardId);
        cert.setFieldName(fieldName);
        cert.setApplicationId(app.getId());
        cert.setStatus(1);
        expertCertMapper.insert(cert);

        SysUser expert = sysUserMapper.selectById(app.getApplicantId());
        if (expert != null) {
            expert.setExpertField(fieldName);
            sysUserMapper.updateById(expert);
        }
    }

    private List<ApplicationDetailDTO> toDetailList(List<Application> apps) {
        Map<Long, String> userNames = new HashMap<>();
        List<Long> userIds = apps.stream()
                .flatMap(a -> java.util.stream.Stream.of(a.getApplicantId(), a.getExpertId()))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            for (SysUser u : sysUserMapper.selectBatchIds(userIds)) {
                userNames.put(u.getId(), u.getRealName());
            }
        }

        Map<Long, String> orgNames = new HashMap<>();
        List<Long> orgIds = apps.stream()
                .map(Application::getOrgId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
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
            dto.setExpertId(app.getExpertId());
            dto.setExpertName(userNames.getOrDefault(app.getExpertId(), ""));
            dto.setCurrentStatus(app.getCurrentStatus());
            String[] statusInfo = STATUS_MAP.getOrDefault(app.getCurrentStatus(), new String[]{"未知", "info"});
            dto.setStatusName(statusInfo[0]);
            dto.setStatusType(statusInfo[1]);
            dto.setRejectReason(app.getRejectReason());
            dto.setAppliedAt(app.getAppliedAt());
            dto.setUpdatedAt(app.getUpdatedAt());
            result.add(dto);
        }
        return result;
    }

    private String statusName(int status) {
        return STATUS_MAP.getOrDefault(status, new String[]{"未知", "info"})[0];
    }
}
