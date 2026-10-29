package com.smartenergy.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class TelemetryPayload {

    @NotNull(message = "deviceId is required")
    private String deviceId;

    @NotNull(message = "voltage is required")
    private Double voltage;

    @NotNull(message = "current is required")
    private Double current;

    private Double power; // optional, can be auto-calculated V * I * PF

    private Double powerFactor = 0.95;

    private LocalDateTime timestamp;

    public TelemetryPayload() {}

    public TelemetryPayload(String deviceId, Double voltage, Double current, Double power, Double powerFactor, LocalDateTime timestamp) {
        this.deviceId = deviceId;
        this.voltage = voltage;
        this.current = current;
        this.power = power;
        this.powerFactor = powerFactor != null ? powerFactor : 0.95;
        this.timestamp = timestamp;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Double getVoltage() {
        return voltage;
    }

    public void setVoltage(Double voltage) {
        this.voltage = voltage;
    }

    public Double getCurrent() {
        return current;
    }

    public void setCurrent(Double current) {
        this.current = current;
    }

    public Double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }

    public Double getPowerFactor() {
        return powerFactor;
    }

    public void setPowerFactor(Double powerFactor) {
        this.powerFactor = powerFactor;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

