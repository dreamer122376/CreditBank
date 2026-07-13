package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 学生证书发放记录。一条记录对应一次认证通过后的正式发证结果。
 */
@TableName("student_cert")
public class StudentCert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long certStandardId;

    private Long applicationId;

    /** 证书编号，对外展示和核验使用 */
    private String certNo;

    /** 学生姓名快照，避免用户资料改名影响历史证书 */
    private String studentName;

    /** 证书名称快照，避免认证标准改名影响历史证书 */
    private String certName;

    /** 发证机构快照 */
    private String orgName;

    /** 简短核验码，后续可扩展成公开核验入口 */
    private String verifyCode;

    /** 1有效，0撤销 */
    private Integer status;

    private String revokeReason;

    private LocalDateTime revokedAt;

    private LocalDateTime issuedAt;

    private LocalDateTime validUntil;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getCertStandardId() {
        return certStandardId;
    }

    public void setCertStandardId(Long certStandardId) {
        this.certStandardId = certStandardId;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getCertNo() {
        return certNo;
    }

    public void setCertNo(String certNo) {
        this.certNo = certNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getCertName() {
        return certName;
    }

    public void setCertName(String certName) {
        this.certName = certName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
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
}
