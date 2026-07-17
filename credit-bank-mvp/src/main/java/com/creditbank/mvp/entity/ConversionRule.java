package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 成果转换规则实体。
 * 定义"原成果 → 转换后成果"的映射关系，关联一条积分规则用于自动加分。
 * 额外注释
 */
@TableName("conversion_rule")
public class ConversionRule {

    @TableId(type = IdType.AUTO)
    private Long id;

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

    @TableField("credit_rule_id")
    private Long creditRuleId;

    @TableField("is_enabled")
    private Integer isEnabled;

    @TableField("effective_start")
    private LocalDateTime effectiveStart;

    @TableField("effective_end")
    private LocalDateTime effectiveEnd;

    private String description;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("created_by")
    private Long createdBy;

    @TableField(exist = false)
    private String originalOrgName;

    @TableField(exist = false)
    private String convertedOrgName;

    @TableField(exist = false)
    private String creditRuleName;

    @TableField(exist = false)
    private Integer creditValue;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getCreditRuleId() {
        return creditRuleId;
    }

    public void setCreditRuleId(Long creditRuleId) {
        this.creditRuleId = creditRuleId;
    }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public LocalDateTime getEffectiveStart() {
        return effectiveStart;
    }

    public void setEffectiveStart(LocalDateTime effectiveStart) {
        this.effectiveStart = effectiveStart;
    }

    public LocalDateTime getEffectiveEnd() {
        return effectiveEnd;
    }

    public void setEffectiveEnd(LocalDateTime effectiveEnd) {
        this.effectiveEnd = effectiveEnd;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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

    public String getCreditRuleName() {
        return creditRuleName;
    }

    public void setCreditRuleName(String creditRuleName) {
        this.creditRuleName = creditRuleName;
    }

    public Integer getCreditValue() {
        return creditValue;
    }

    public void setCreditValue(Integer creditValue) {
        this.creditValue = creditValue;
    }
}