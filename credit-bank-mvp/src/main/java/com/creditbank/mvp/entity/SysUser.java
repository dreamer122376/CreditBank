package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@TableName("sys_user")
@Schema(description = "系统用户")
public class SysUser {

    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "登录账号", example = "admin")
    private String username;

    @Schema(description = "加密后的密码", hidden = true)
    private String password;

    @Schema(description = "真实姓名", example = "系统管理员")
    private String realName;

    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "电子邮箱", example = "admin@creditbank.com")
    private String email;

    @Schema(description = "角色：admin/org_admin/expert/student", example = "admin")
    private String role;

    @Schema(description = "所属机构ID", example = "1")
    private Long orgId;

    @Schema(description = "专家擅长领域", example = "计算机科学")
    private String expertField;

    @Schema(description = "当前可用总积分", example = "500")
    private Integer balance;

    @Schema(description = "账号状态：1正常，0冻结", example = "1")
    private Integer status;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginAt;

    @Schema(description = "注册时间")
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getExpertField() {
        return expertField;
    }

    public void setExpertField(String expertField) {
        this.expertField = expertField;
    }

    public Integer getBalance() {
        return balance;
    }

    public void setBalance(Integer balance) {
        this.balance = balance;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @TableField(exist = false)
    private String orgName;

    @TableField(exist = false)
    private List<Project> projects;

    @TableField(exist = false)
    private List<StudentProject> studentProjects;

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public List<StudentProject> getStudentProjects() {
        return studentProjects;
    }

    public void setStudentProjects(List<StudentProject> studentProjects) {
        this.studentProjects = studentProjects;
    }
}
