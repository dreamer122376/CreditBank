package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@TableName("transaction_log")
@Schema(description = "交易日志")
public class TransactionLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID", example = "1")
    private Long id;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "变动积分", example = "100")
    private Integer amount;

    @Schema(description = "变动后余额", example = "500")
    private Integer balanceAfter;

    @Schema(description = "业务类型：EARN/SPEND", example = "EARN")
    private String bizType;

    @Schema(description = "业务ID", example = "project_1")
    /** 相关规则ID：REWARD对应积分规则id，EXCHANGE对应兑换规则id，REFUND对应原流水id，ADMIN无意义 */
    private Long relatedRuleId;

    @Schema(description = "变动描述", example = "完成项目获得积分")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @TableField(exist = false)
    @Schema(description = "是否已被撤销")
    private Boolean reverted;

    @TableField(exist = false)
    @Schema(description = "用户名")
    private String userName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(Integer balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getBizType() {
        return bizType;
    }

    public void setBizType(String bizType) {
        this.bizType = bizType;
    }

    public Long getRelatedRuleId() {
        return relatedRuleId;
    }

    public void setRelatedRuleId(Long relatedRuleId) {
        this.relatedRuleId = relatedRuleId;
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

    public Boolean getReverted() {
        return reverted;
    }

    public void setReverted(Boolean reverted) {
        this.reverted = reverted;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
