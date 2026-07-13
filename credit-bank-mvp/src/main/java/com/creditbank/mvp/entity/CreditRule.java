package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@TableName("credit_rule")
@Schema(description = "积分规则")
public class CreditRule {

    @TableId(type = IdType.AUTO)
    @Schema(description = "规则ID", example = "1")
    private Long id;

    @Schema(description = "事件代码", example = "project_complete")
    private String eventCode;

    @Schema(description = "事件名称", example = "完成项目")
    private String eventName;

    @Schema(description = "基础积分值", example = "100")
    private Integer creditValue;

    @Schema(description = "关联项目ID", example = "1")
    private Long projectId;

    @Schema(description = "是否启用：1启用，0禁用", example = "1")
    private Integer isEnabled;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "最后修改时间")
    private LocalDateTime updatedAt;

    @Schema(description = "规则生效开始时间")
    private LocalDateTime startTime;

    @Schema(description = "规则生效结束时间")
    private LocalDateTime endTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Integer getCreditValue() {
        return creditValue;
    }

    public void setCreditValue(Integer creditValue) {
        this.creditValue = creditValue;
    }

    public Long getProjectId() { return projectId; }

    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Integer getIsEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Integer isEnabled) {
        this.isEnabled = isEnabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
