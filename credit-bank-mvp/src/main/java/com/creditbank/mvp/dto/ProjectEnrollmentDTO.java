package com.creditbank.mvp.dto;

import java.time.LocalDateTime;

/**
 * 学生已报名项目 DTO。
 * 包含项目基本信息 + 报名记录状态 + 报名时间。
 */
public class ProjectEnrollmentDTO {

    private Long enrollmentId;
    private Long projectId;
    private String projectName;
    private String description;
    private Integer creditReward;
    private Integer creditPrice;
    private String status;
    private String orgName;
    private String expertName;
    private LocalDateTime enrolledAt;
    private LocalDateTime projectCreatedAt;

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCreditReward() {
        return creditReward;
    }

    public void setCreditReward(Integer creditReward) {
        this.creditReward = creditReward;
    }

    public Integer getCreditPrice() {
        return creditPrice;
    }

    public void setCreditPrice(Integer creditPrice) {
        this.creditPrice = creditPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getExpertName() {
        return expertName;
    }

    public void setExpertName(String expertName) {
        this.expertName = expertName;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public LocalDateTime getProjectCreatedAt() {
        return projectCreatedAt;
    }

    public void setProjectCreatedAt(LocalDateTime projectCreatedAt) {
        this.projectCreatedAt = projectCreatedAt;
    }
}
