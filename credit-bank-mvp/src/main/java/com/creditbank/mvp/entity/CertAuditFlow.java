package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 认证审批流程节点。每条记录代表审批链中的一个节点，
 * 通过 next_node_id 串成链表，末节点的 next_node_id = NULL 表示审批通过。
 */
@TableName("cert_audit_flow")
public class CertAuditFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long certStandardId;

    /** 审核人员用户ID（sys_user.id） */
    private Long auditorId;

    /** 下一步流程节点ID（NULL 表示流程结束，即审批通过） */
    private Long nextNodeId;

    private LocalDateTime createdAt;

    // ---- 非持久化字段（用于前端展示） ----

    /** 审核人姓名 */
    @TableField(exist = false)
    private String auditorName;

    /** 审核人角色 */
    @TableField(exist = false)
    private String auditorRole;

    /** 是否为末节点（next_node_id 为空） */
    @TableField(exist = false)
    private Boolean isFinal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCertStandardId() {
        return certStandardId;
    }

    public void setCertStandardId(Long certStandardId) {
        this.certStandardId = certStandardId;
    }

    public Long getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(Long auditorId) {
        this.auditorId = auditorId;
    }

    public Long getNextNodeId() {
        return nextNodeId;
    }

    public void setNextNodeId(Long nextNodeId) {
        this.nextNodeId = nextNodeId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAuditorName() {
        return auditorName;
    }

    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
    }

    public String getAuditorRole() {
        return auditorRole;
    }

    public void setAuditorRole(String auditorRole) {
        this.auditorRole = auditorRole;
    }

    public Boolean getIsFinal() {
        return isFinal;
    }

    public void setIsFinal(Boolean isFinal) {
        this.isFinal = isFinal;
    }
}
