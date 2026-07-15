package com.creditbank.mvp.dto;

public class PublishNotificationResult {
    private String scopeType;
    private Long orgId;
    private int recipientCount;

    public PublishNotificationResult(String scopeType, Long orgId, int recipientCount) {
        this.scopeType = scopeType;
        this.orgId = orgId;
        this.recipientCount = recipientCount;
    }

    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public int getRecipientCount() { return recipientCount; }
    public void setRecipientCount(int recipientCount) { this.recipientCount = recipientCount; }
}
