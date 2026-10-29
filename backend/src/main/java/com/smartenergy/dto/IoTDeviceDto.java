package com.smartenergy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class IoTDeviceDto {
    private Long id;

    @NotBlank(message = "Unique Device ID is required (e.g. ESP32-001)")
    private String deviceId;

    @NotBlank(message = "Device name is required")
    private String name;

    private String deviceType = "ESP32_PZEM004T";

    @NotNull(message = "Zone ID is required")
    private Long zoneId;

    private String zoneName;
    private Long buildingId;
    private String buildingName;

    private String protocol = "MQTT";
    private String firmwareVersion = "1.0.0-PROD";
    private LocalDateTime lastSeenAt;
    private boolean isOnline = false;
    private boolean active = true;
    private boolean simulationEnabled = true;

    // Latest readings cache
    private Double currentVoltage;
    private Double currentAmperes;
    private Double currentPowerWatts;
    private Double totalEnergyKwh;

    private LocalDateTime createdAt;

    public IoTDeviceDto() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public Long getBuildingId() {
        return buildingId;
    }

    public void setBuildingId(Long buildingId) {
        this.buildingId = buildingId;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public LocalDateTime getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(LocalDateTime lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSimulationEnabled() {
        return simulationEnabled;
    }

    public void setSimulationEnabled(boolean simulationEnabled) {
        this.simulationEnabled = simulationEnabled;
    }

    public Double getCurrentVoltage() {
        return currentVoltage;
    }

    public void setCurrentVoltage(Double currentVoltage) {
        this.currentVoltage = currentVoltage;
    }

    public Double getCurrentAmperes() {
        return currentAmperes;
    }

    public void setCurrentAmperes(Double currentAmperes) {
        this.currentAmperes = currentAmperes;
    }

    public Double getCurrentPowerWatts() {
        return currentPowerWatts;
    }

    public void setCurrentPowerWatts(Double currentPowerWatts) {
        this.currentPowerWatts = currentPowerWatts;
    }

    public Double getTotalEnergyKwh() {
        return totalEnergyKwh;
    }

    public void setTotalEnergyKwh(Double totalEnergyKwh) {
        this.totalEnergyKwh = totalEnergyKwh;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

