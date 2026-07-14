package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 专家认证记录。一条记录 = 专家持有某个认证标准的评审资质，
 * 既是资料页展示的"徽章"，也是指派评审专家时的资质依据。
 */
@TableName("expert_cert")
public class ExpertCert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long expertId;

    private Long certStandardId;

    /** 认证领域名快照（标准改名不影响已发记录） */
    private String fieldName;

    /** 来源申请单ID，可溯源 */
    private Long applicationId;

    /** 1有效，0撤销 */
    private Integer status;

    private LocalDateTime issuedAt;

    /** 资质有效期截止，NULL 为长期有效 */
    private LocalDateTime validUntil;

    private String revokeReason;

    private LocalDateTime revokedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExpertId() {
        return expertId;
    }

    public void setExpertId(Long expertId) {
        this.expertId = expertId;
    }

    public Long getCertStandardId() {
        return certStandardId;
    }

    public void setCertStandardId(Long certStandardId) {
        this.certStandardId = certStandardId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(LocalDateTime validUntil) {
        this.validUntil = validUntil;
    }

    public String getRevokeReason() {
        return revokeReason;
    }

    public void setRevokeReason(String revokeReason) {
        this.revokeReason = revokeReason;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
}
