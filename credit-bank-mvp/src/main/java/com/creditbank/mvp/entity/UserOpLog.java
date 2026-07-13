package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/**
 * 用户操作日志。对应表 user_op_log。
 * 记录管理员对用户的所有管理操作（冻结/解冻/重置密码/批量操作）。
 * 扩展记录 CAMPAIGN/PROJECT/POINT/ENROLL 等核心业务操作。
 */
@TableName("user_op_log")
public class UserOpLog {

    // ========== 模块常量 ==========
    public static final String MODULE_USER = "USER";
    public static final String MODULE_CAMPAIGN = "CAMPAIGN";
    public static final String MODULE_PROJECT = "PROJECT";
    public static final String MODULE_POINT = "POINT";
    public static final String MODULE_ENROLL = "ENROLL";

    // ========== 操作类型常量 ==========
    public static final String ACTION_FREEZE = "FREEZE";
    public static final String ACTION_UNFREEZE = "UNFREEZE";
    public static final String ACTION_BATCH_FREEZE = "BATCH_FREEZE";
    public static final String ACTION_BATCH_UNFREEZE = "BATCH_UNFREEZE";
    public static final String ACTION_RESET_PW = "RESET_PW";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_PROJECT_AUDIT = "PROJECT_AUDIT";
    public static final String ACTION_PROJECT_OFFLINE = "PROJECT_OFFLINE";
    public static final String ACTION_CAMPAIGN_ENROLL = "CAMPAIGN_ENROLL";
    public static final String ACTION_CAMPAIGN_LEAVE = "CAMPAIGN_LEAVE";
    public static final String ACTION_PROJECT_ENROLL = "PROJECT_ENROLL";
    public static final String ACTION_PROJECT_LEAVE = "PROJECT_LEAVE";
    public static final String ACTION_EARN = "EARN";

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作人ID */
    private Long operatorId;

    /** 操作人姓名（冗余，方便直接展示） */
    private String operatorName;

    /** 被操作的用户ID */
    private Long targetUserId;

    /** 被操作的用户名（冗余） */
    private String targetUserName;

    /** 操作类型 */
    private String action;

    /** 模块：USER/CAMPAIGN/PROJECT/POINT/ENROLL */
    private String module;

    /** 操作详情描述 */
    private String detail;

    private LocalDateTime createdAt;

    /**
     * 构造一条操作日志。
     */
    public static UserOpLog createLog(Long operatorId, String operatorName,
                                      Long targetUserId, String targetUserName,
                                      String module, String action, String detail) {
        UserOpLog log = new UserOpLog();
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setTargetUserId(targetUserId);
        log.setTargetUserName(targetUserName);
        log.setModule(module);
        log.setAction(action);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }

    // ========== getters & setters ==========

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public void setTargetUserName(String targetUserName) {
        this.targetUserName = targetUserName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
