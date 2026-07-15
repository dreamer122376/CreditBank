package com.creditbank.mvp.dto;

public class PublishNotificationRequest {
    private String scopeType;
    private Long orgId;
    private String level;
    private String title;
    private String content;

    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public Long getOrgId() { return orgId; }
    public void setOrgId(Long orgId) { this.orgId = orgId; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
