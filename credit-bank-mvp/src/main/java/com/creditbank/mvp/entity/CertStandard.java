package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("cert_standard")
public class CertStandard {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String standardName;

    private Integer minCredit;

    private Integer needExpertApprove;

    private Integer validityDays;

    private Integer isEnabled;

    private LocalDateTime createdAt;

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

    public Integer getMinCredit() {
        return minCredit;
    }

    public void setMinCredit(Integer minCredit) {
        this.minCredit = minCredit;
    }

    public Integer getNeedExpertApprove() {
        return needExpertApprove;
    }

    public void setNeedExpertApprove(Integer needExpertApprove) {
        this.needExpertApprove = needExpertApprove;
    }

    public Integer getValidityDays() {
        return validityDays;
    }

    public void setValidityDays(Integer validityDays) {
        this.validityDays = validityDays;
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
}
