package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 积分交易流水。对应表 t_point_transaction。
 * 账本核心铁律：余额的每一次变动，都必须落一条不可修改的流水，
 * 记录变动多少（changeAmount）和变动后的余额（balanceAfter），
 * 这样余额永远可以用流水逐条对账、追溯。
 */
@TableName("t_point_transaction")
public class PointTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long accountId;

    private String ruleCode;

    /** 本次变动积分，正数为加、负数为减 */
    private BigDecimal changeAmount;

    /** 变动后的余额快照 */
    private BigDecimal balanceAfter;

    private String remark;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public BigDecimal getChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(BigDecimal changeAmount) {
        this.changeAmount = changeAmount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
