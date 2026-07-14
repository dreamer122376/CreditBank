package com.creditbank.mvp.dto;

import java.time.LocalDateTime;

/**
 * 项目列表项 DTO。
 * 学生端/管理端列表使用，包含机构和专家名称等冗余字段，避免前端多次请求。
 */
public class ProjectListDTO {

    private Long id;
    private String name;
    private String description;
    private Integer creditReward;
    private Integer creditPrice;
    private Integer status;
    private String statusName;
    private Long orgId;
    private String orgName;
    private Long expertId;
    private String expertName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 当前学生是否已报名（学生端列表时填充） */
    private Boolean enrolled;

    /** 当前学生的报名状态（学生端列表时填充） */
    private String enrollmentStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(Boolean enrolled) {
        this.enrolled = enrolled;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public void setEnrollmentStatus(String enrollmentStatus) {
        this.enrollmentStatus = enrollmentStatus;
    }
}
