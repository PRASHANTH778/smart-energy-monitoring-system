package com.smartenergy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AutomationRuleDto {
    private Long id;

    @NotBlank(message = "Rule name is required")
    private String name;

    private String description;
    private String targetType = "ALL"; // ALL, BUILDING, ZONE, DEVICE
    private Long targetId;
    private String targetName;

    @NotBlank(message = "Condition type is required")
    private String conditionType; // POWER_THRESHOLD_EXCEEDED, DAILY_TARGET_EXCEEDED, DEVICE_OFFLINE, ABNORMAL_CONSUMPTION

    @NotNull(message = "Threshold value is required")
    private Double thresholdValue;

    private Integer evaluationIntervalSeconds = 30;
    private Integer cooldownMinutes = 15;
    private String actionType = "SIMULATED_LOAD_SHEDDING";
    private boolean enabled = true;
    private LocalDateTime lastEvaluatedAt;
    private LocalDateTime lastTriggeredAt;
    private LocalDateTime createdAt;

    public AutomationRuleDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public Double getThresholdValue() {
        return thresholdValue;
    }

    public void setThresholdValue(Double thresholdValue) {
        this.thresholdValue = thresholdValue;
    }

    public Integer getEvaluationIntervalSeconds() {
        return evaluationIntervalSeconds;
    }

    public void setEvaluationIntervalSeconds(Integer evaluationIntervalSeconds) {
        this.evaluationIntervalSeconds = evaluationIntervalSeconds;
    }

    public Integer getCooldownMinutes() {
        return cooldownMinutes;
    }

    public void setCooldownMinutes(Integer cooldownMinutes) {
        this.cooldownMinutes = cooldownMinutes;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getLastEvaluatedAt() {
        return lastEvaluatedAt;
    }

    public void setLastEvaluatedAt(LocalDateTime lastEvaluatedAt) {
        this.lastEvaluatedAt = lastEvaluatedAt;
    }

    public LocalDateTime getLastTriggeredAt() {
        return lastTriggeredAt;
    }

    public void setLastTriggeredAt(LocalDateTime lastTriggeredAt) {
        this.lastTriggeredAt = lastTriggeredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

