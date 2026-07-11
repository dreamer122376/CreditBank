package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.ApplicationDetailDTO;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.ApplicationMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
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

        STATUS_MAP.put(STATUS_DRAFT, new String[]{"草稿", "info"});
        STATUS_MAP.put(STATUS_ORG_AUDIT, new String[]{"待机构审核", "warning"});
        STATUS_MAP.put(STATUS_EXPERT_AUDIT, new String[]{"待专家评审", "warning"});
        STATUS_MAP.put(STATUS_APPROVED, new String[]{"已通过", "success"});
        STATUS_MAP.put(STATUS_REJECTED, new String[]{"已驳回", "danger"});
    }

    private final ApplicationMapper applicationMapper;
    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;

    public ApplicationService(ApplicationMapper applicationMapper,
                              SysUserMapper sysUserMapper,
                              OrganizationMapper organizationMapper) {
        this.applicationMapper = applicationMapper;
        this.sysUserMapper = sysUserMapper;
        this.organizationMapper = organizationMapper;
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

    /** 提交申请，直接进入"待机构审核" */
    public Application submit(Application app) {
        if (app.getBizType() == null || !BIZ_TYPE_NAME.containsKey(app.getBizType())) {
            throw new BizException("非法的业务类型：" + app.getBizType());
        }
        if (app.getApplicantId() == null || sysUserMapper.selectById(app.getApplicantId()) == null) {
            throw new BizException("申请人不存在：" + app.getApplicantId());
        }
        app.setId(null);
        app.setCurrentStatus(STATUS_ORG_AUDIT);
        app.setRejectReason(null);
        applicationMapper.insert(app);
        return applicationMapper.selectById(app.getId());
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
        if (orgStage && !("org_admin".equals(role) || "admin".equals(role))) {
            throw new BizException("待机构审核的申请只能由机构管理员处理");
        }
        if (expertStage && !("expert".equals(role) || "admin".equals(role))) {
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
        return app;
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
