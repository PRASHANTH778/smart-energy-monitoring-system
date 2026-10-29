package com.smartenergy.dto;

import jakarta.validation.constraints.NotBlank;

public class AlertActionRequest {
    @NotBlank(message = "Action is required (ACKNOWLEDGE or RESOLVE)")
    private String action; // ACKNOWLEDGE, RESOLVE

    private String notes;

    public AlertActionRequest() {}

    public AlertActionRequest(String action, String notes) {
        this.action = action;
        this.notes = notes;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

