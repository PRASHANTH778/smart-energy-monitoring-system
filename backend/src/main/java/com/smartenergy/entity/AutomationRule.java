package com.smartenergy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "automation_rules")
public class AutomationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "target_type", nullable = false, length = 30)
    private String targetType = "ALL"; // ALL, BUILDING, ZONE, DEVICE

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "target_name", length = 100)
    private String targetName;

    @Column(name = "condition_type", nullable = false, length = 50)
    private String conditionType; // POWER_THRESHOLD_EXCEEDED, DAILY_TARGET_EXCEEDED, DEVICE_OFFLINE, ABNORMAL_CONSUMPTION

    @Column(name = "threshold_value", nullable = false)
    private Double thresholdValue;

    @Column(name = "evaluation_interval_seconds", nullable = false)
    private Integer evaluationIntervalSeconds = 30;

    @Column(name = "cooldown_minutes", nullable = false)
    private Integer cooldownMinutes = 15;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType = "SIMULATED_LOAD_SHEDDING"; // SIMULATED_LOAD_SHEDDING, ALERT_ONLY, AUTO_DIM_LIGHTING_SIMULATED, SET_ECO_MODE_SIMULATED

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(name = "last_evaluated_at")
    private LocalDateTime lastEvaluatedAt;

    @Column(name = "last_triggered_at")
    private LocalDateTime lastTriggeredAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public AutomationRule() {}

    public AutomationRule(String name, String description, String targetType, Long targetId, String targetName, String conditionType, Double thresholdValue, Integer cooldownMinutes, String actionType) {
        this.name = name;
        this.description = description;
        this.targetType = targetType != null ? targetType : "ALL";
        this.targetId = targetId;
        this.targetName = targetName;
        this.conditionType = conditionType;
        this.thresholdValue = thresholdValue;
        this.cooldownMinutes = cooldownMinutes != null ? cooldownMinutes : 15;
        this.actionType = actionType != null ? actionType : "SIMULATED_LOAD_SHEDDING";
        this.enabled = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

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

