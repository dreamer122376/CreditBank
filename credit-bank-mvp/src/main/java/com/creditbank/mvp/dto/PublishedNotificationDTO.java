package com.creditbank.mvp.dto;

import java.time.LocalDateTime;

public class PublishedNotificationDTO {
    private Long id;
    private String title;
    private String content;
    private String level;
    private String scopeType;
    private String scopeValue;
    private String scopeName;
    private Long actorId;
    private String actorName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private long recipientCount;
    private long readCount;
    private long confirmedCount;
    private boolean canRevoke;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getScopeType() { return scopeType; }
    public void setScopeType(String scopeType) { this.scopeType = scopeType; }
    public String getScopeValue() { return scopeValue; }
    public void setScopeValue(String scopeValue) { this.scopeValue = scopeValue; }
    public String getScopeName() { return scopeName; }
    public void setScopeName(String scopeName) { this.scopeName = scopeName; }
    public Long getActorId() { return actorId; }
    public void setActorId(Long actorId) { this.actorId = actorId; }
    public String getActorName() { return actorName; }
    public void setActorName(String actorName) { this.actorName = actorName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public long getRecipientCount() { return recipientCount; }
    public void setRecipientCount(long recipientCount) { this.recipientCount = recipientCount; }
    public long getReadCount() { return readCount; }
    public void setReadCount(long readCount) { this.readCount = readCount; }
    public long getConfirmedCount() { return confirmedCount; }
    public void setConfirmedCount(long confirmedCount) { this.confirmedCount = confirmedCount; }
    public boolean isCanRevoke() { return canRevoke; }
    public void setCanRevoke(boolean canRevoke) { this.canRevoke = canRevoke; }
}
