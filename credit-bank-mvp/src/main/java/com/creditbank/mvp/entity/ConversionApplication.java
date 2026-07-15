package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("conversion_application")
public class ConversionApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("rule_id")
    private Long ruleId;

    @TableField("student_id")
    private Long studentId;

    @TableField("original_name")
    private String originalName;

    @TableField("original_org_id")
    private Long originalOrgId;

    @TableField("original_type")
    private String originalType;

    @TableField("converted_name")
    private String convertedName;

    @TableField("converted_org_id")
    private Long convertedOrgId;

    @TableField("converted_type")
    private String convertedType;

    @TableField("certificate_file")
    private String certificateFile;

    @TableField("apply_type")
    private String applyType;

    private Integer status;

    @TableField("reject_reason")
    private String rejectReason;

    @TableField("approved_at")
    private LocalDateTime approvedAt;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String originalOrgName;

    @TableField(exist = false)
    private String convertedOrgName;

    @TableField(exist = false)
    private String ruleName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Long getOriginalOrgId() {
        return originalOrgId;
    }

    public void setOriginalOrgId(Long originalOrgId) {
        this.originalOrgId = originalOrgId;
    }

    public String getOriginalType() {
        return originalType;
    }

    public void setOriginalType(String originalType) {
        this.originalType = originalType;
    }

    public String getConvertedName() {
        return convertedName;
    }

    public void setConvertedName(String convertedName) {
        this.convertedName = convertedName;
    }

    public Long getConvertedOrgId() {
        return convertedOrgId;
    }

    public void setConvertedOrgId(Long convertedOrgId) {
        this.convertedOrgId = convertedOrgId;
    }

    public String getConvertedType() {
        return convertedType;
    }

    public void setConvertedType(String convertedType) {
        this.convertedType = convertedType;
    }

    public String getCertificateFile() {
        return certificateFile;
    }

    public void setCertificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
    }

    public String getApplyType() {
        return applyType;
    }

    public void setApplyType(String applyType) {
        this.applyType = applyType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getOriginalOrgName() {
        return originalOrgName;
    }

    public void setOriginalOrgName(String originalOrgName) {
        this.originalOrgName = originalOrgName;
    }

    public String getConvertedOrgName() {
        return convertedOrgName;
    }

    public void setConvertedOrgName(String convertedOrgName) {
        this.convertedOrgName = convertedOrgName;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
}