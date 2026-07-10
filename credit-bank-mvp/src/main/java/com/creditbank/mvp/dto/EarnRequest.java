package com.creditbank.mvp.dto;

/**
 * 加分请求参数。用 DTO 接收前端入参，而不是直接用实体，
 * 是为了让"对外的接口契约"和"数据库表结构"解耦。
 */
public class EarnRequest {

    private Long accountId;

    private String ruleCode;

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
}
