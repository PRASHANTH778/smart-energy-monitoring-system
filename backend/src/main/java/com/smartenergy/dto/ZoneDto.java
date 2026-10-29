package com.smartenergy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class ZoneDto {
    private Long id;

    @NotBlank(message = "Zone name is required")
    private String name;

    private String description;

    @NotNull(message = "Building ID is required")
    private Long buildingId;

    private String buildingName;

    private Double dailyEnergyTargetKwh = 100.0;
    private boolean active = true;
    private int deviceCount;
    private Double currentConsumptionTodayKwh;
    private LocalDateTime createdAt;

    public ZoneDto() {}

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

    public Double getDailyEnergyTargetKwh() {
        return dailyEnergyTargetKwh;
    }

    public void setDailyEnergyTargetKwh(Double dailyEnergyTargetKwh) {
        this.dailyEnergyTargetKwh = dailyEnergyTargetKwh;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public Double getCurrentConsumptionTodayKwh() {
        return currentConsumptionTodayKwh;
    }

    public void setCurrentConsumptionTodayKwh(Double currentConsumptionTodayKwh) {
        this.currentConsumptionTodayKwh = currentConsumptionTodayKwh;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

