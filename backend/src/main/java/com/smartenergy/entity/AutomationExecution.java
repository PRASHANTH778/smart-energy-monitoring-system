package com.smartenergy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "automation_executions")
public class AutomationExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rule_id", nullable = false)
    private AutomationRule rule;

    @Column(name = "trigger_reason", nullable = false, length = 255)
    private String triggerReason;

    @Column(name = "action_taken", nullable = false, length = 255)
    private String actionTaken;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "SIMULATED_SUCCESS";

    @Column(length = 500)
    private String details;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    public AutomationExecution() {}

    public AutomationExecution(AutomationRule rule, String triggerReason, String actionTaken, String status, String details) {
        this.rule = rule;
        this.triggerReason = triggerReason;
        this.actionTaken = actionTaken;
        this.status = status != null ? status : "SIMULATED_SUCCESS";
        this.details = details;
        this.executedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.executedAt == null) {
            this.executedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AutomationRule getRule() {
        return rule;
    }

    public void setRule(AutomationRule rule) {
        this.rule = rule;
    }

    public String getTriggerReason() {
        return triggerReason;
    }

    public void setTriggerReason(String triggerReason) {
        this.triggerReason = triggerReason;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}

