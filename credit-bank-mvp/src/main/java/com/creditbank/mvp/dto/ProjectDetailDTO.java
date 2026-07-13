package com.creditbank.mvp.dto;

import com.creditbank.mvp.entity.SysUser;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目详情 DTO。
 * 包含项目完整信息、主办机构、负责专家，以及已报名学生列表（管理端/机构端查看）。
 */
public class ProjectDetailDTO {

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

    /** 已报名学生列表，只有机构/管理端查询时才填充 */
    private List<SysUser> enrolledStudents;

    /** 当前登录学生是否已报名（学生端查看时填充） */
    private Boolean enrolled;

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

    public List<SysUser> getEnrolledStudents() {
        return enrolledStudents;
    }

    public void setEnrolledStudents(List<SysUser> enrolledStudents) {
        this.enrolledStudents = enrolledStudents;
    }

    public Boolean getEnrolled() {
        return enrolled;
    }

    public void setEnrolled(Boolean enrolled) {
        this.enrolled = enrolled;
    }
}
