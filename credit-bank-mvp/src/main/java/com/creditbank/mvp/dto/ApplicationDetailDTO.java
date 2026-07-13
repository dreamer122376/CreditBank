package com.creditbank.mvp.dto;

import java.time.LocalDateTime;

/** 业务流程列表项：application 记录 + 关联的申请人/机构/专家名称 */
public class ApplicationDetailDTO {

    private Long id;
    private String bizType;
    private String bizTypeName;
    private Long applicantId;
    private String applicantName;
    private Long orgId;
    private String orgName;
    private Long expertId;
    private String expertName;
    private Integer currentStatus;
    private String statusName;
    private String statusType;
    private String rejectReason;
    private String formData;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    /** 当前登录人是否可审批本单（后端按角色/审批链算好，前端直接用） */
    private Boolean canAudit;
    /** 认证业务：当前停留节点及审核人 */
    private Long currentNodeId;
    private String currentAuditorName;
    /** 认证业务：完整审批链及各步状态 */
    private java.util.List<FlowStepDTO> flowSteps;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public String getBizTypeName() {
        return bizTypeName;
    }

    public void setBizTypeName(String bizTypeName) {
        this.bizTypeName = bizTypeName;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public Long getExpertId() {
        return expertId;
    }

    public void setExpertId(Long expertId) {
        this.expertId = expertId;
    }

    public String getExpertName() {
        return expertName;
    }

    public void setExpertName(String expertName) {
        this.expertName = expertName;
    }

    public Integer getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Integer currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getStatusType() {
        return statusType;
    }

    public void setStatusType(String statusType) {
        this.statusType = statusType;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getCanAudit() {
        return canAudit;
    }

    public void setCanAudit(Boolean canAudit) {
        this.canAudit = canAudit;
    }

    public Long getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(Long currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public String getCurrentAuditorName() {
        return currentAuditorName;
    }

    public void setCurrentAuditorName(String currentAuditorName) {
        this.currentAuditorName = currentAuditorName;
    }

    public java.util.List<FlowStepDTO> getFlowSteps() {
        return flowSteps;
    }

    public void setFlowSteps(java.util.List<FlowStepDTO> flowSteps) {
        this.flowSteps = flowSteps;
    }
}
