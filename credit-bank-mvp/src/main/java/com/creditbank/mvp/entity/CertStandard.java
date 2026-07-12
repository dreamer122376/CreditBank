package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("cert_standard")
public class CertStandard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String standardName;

    private String version;

    private Long orgId;

    /** 适用对象：student/expert/org_admin（与 sys_user.role 枚举一致） */
    private String targetRole;

    /** 认证要求表述（执行标准正文） */
    private String requirementText;

    /** 是否需要人工审核：0否（自动通过） 1是 */
    private Integer needManualAudit;

    /** 审批流程第一步节点ID（仅 needManualAudit=1 时有效） */
    private Long firstNodeId;

    private Integer isEnabled;

    private LocalDateTime createdAt;

    // ---- 非持久化字段（用于前端展示） ----

    /** 归属机构名称（org_id 关联查询） */
    @TableField(exist = false)
    private String orgName;

    /** 第一步审核人姓名（first_node_id → auditor_id 关联查询） */
    @TableField(exist = false)
    private String firstAuditorName;

    /** 流程节点总数 */
    @TableField(exist = false)
    private Integer flowStepCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStandardName() {
        return standardName;
    }

    public void setStandardName(String standardName) {
        this.standardName = standardName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public String getRequirementText() {
        return requirementText;
    }

    public void setRequirementText(String requirementText) {
        this.requirementText = requirementText;
    }

    public Integer getNeedManualAudit() {
        return needManualAudit;
    }

    public void setNeedManualAudit(Integer needManualAudit) {
        this.needManualAudit = needManualAudit;
    }

    public Long getFirstNodeId() {
        return firstNodeId;
    }

    public void setFirstNodeId(Long firstNodeId) {
        this.firstNodeId = firstNodeId;
    }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getFirstAuditorName() {
        return firstAuditorName;
    }

    public void setFirstAuditorName(String firstAuditorName) {
        this.firstAuditorName = firstAuditorName;
    }

    public Integer getFlowStepCount() {
        return flowStepCount;
    }

    public void setFlowStepCount(Integer flowStepCount) {
        this.flowStepCount = flowStepCount;
    }
}
