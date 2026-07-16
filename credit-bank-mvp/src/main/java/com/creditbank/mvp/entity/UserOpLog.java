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
    // 转换规则模块：记录创建/编辑/删除/启停等管理操作
    public static final String MODULE_CONVERSION_RULE = "CONVERSION_RULE";
    // 成果转换申请：学生提交转换申请、管理员审核
    public static final String MODULE_CONVERSION_APPLY = "CONVERSION_APPLY";
    // 证书申请：学生提交证书认证、管理员审核
    public static final String MODULE_APPLICATION = "APPLICATION";
    // 机构注册申请：机构提交注册、管理员审核
    public static final String MODULE_ORG_REGISTER = "ORG_REGISTER";
    // 学生证书：发放、撤回
    public static final String MODULE_STUDENT_CERT = "STUDENT_CERT";
    // 兑换规则管理：创建/编辑/启停（注意与 MODULE_POINT.EXCHANGE 区分：前者是后台配置管理，后者是用户行为）
    public static final String MODULE_EXCHANGE_RULE = "EXCHANGE_RULE";
    // 积分规则管理：创建/编辑/删除/启停/批量调账
    public static final String MODULE_CREDIT_RULE = "CREDIT_RULE";
    // 证书标准：创建/编辑/启停
    public static final String MODULE_CERT_STANDARD = "CERT_STANDARD";
    // 机构管理：创建/编辑/审核通过/驳回
    public static final String MODULE_ORGANIZATION = "ORGANIZATION";

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
    public static final String ACTION_PROJECT_APPROVE = "PROJECT_APPROVE";
    public static final String ACTION_PROJECT_REJECT = "PROJECT_REJECT";
    public static final String ACTION_PROJECT_OFFLINE = "PROJECT_OFFLINE";
    public static final String ACTION_CAMPAIGN_ENROLL = "CAMPAIGN_ENROLL";
    public static final String ACTION_CAMPAIGN_LEAVE = "CAMPAIGN_LEAVE";
    public static final String ACTION_PROJECT_ENROLL = "PROJECT_ENROLL";
    public static final String ACTION_PROJECT_LEAVE = "PROJECT_LEAVE";
    public static final String ACTION_PROJECT_SUBMIT = "PROJECT_SUBMIT";
    public static final String ACTION_EARN = "EARN";
    // 积分兑换动作（与 TransactionLog.bizType=EXCHANGE 对齐，修复之前硬编码字符串的问题）
    public static final String ACTION_EXCHANGE = "EXCHANGE";
    // 转换规则启停动作（历史常量，仅 ConversionRuleService 专用）
    public static final String ACTION_CONVERSION_TOGGLE = "CONVERSION_TOGGLE";
    // 通用：配置类规则启停（兑换规则、积分规则、证书标准通用，module 区分维度）
    public static final String ACTION_RULE_TOGGLE = "RULE_TOGGLE";
    // 通用：申请/注册/提交（证书申请、机构注册、转换申请均复用）
    public static final String ACTION_SUBMIT = "SUBMIT";
    // 通用：审批通过（证书申请、机构注册、转换申请、机构审核均复用，module 区分维度）
    public static final String ACTION_APPROVE = "APPROVE";
    // 通用：审批驳回
    public static final String ACTION_REJECT = "REJECT";
    // 证书发放
    public static final String ACTION_ISSUE = "ISSUE";
    // 证书/资质撤回
    public static final String ACTION_REVOKE = "REVOKE";
    // 积分批量调账（按积分规则追溯调整历史数据）
    public static final String ACTION_BATCH_ADJUST = "BATCH_ADJUST";

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
