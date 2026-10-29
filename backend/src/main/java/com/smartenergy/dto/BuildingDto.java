package com.smartenergy.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class BuildingDto {
    private Long id;

    @NotBlank(message = "Building name is required")
    private String name;

    private String description;
    private String location;
    private String buildingType;
    private boolean active = true;
    private int zoneCount;
    private int deviceCount;
    private LocalDateTime createdAt;

    public BuildingDto() {}

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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getBuildingType() {
        return buildingType;
    }

    public void setBuildingType(String buildingType) {
        this.buildingType = buildingType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getZoneCount() {
        return zoneCount;
    }

    public void setZoneCount(int zoneCount) {
        this.zoneCount = zoneCount;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

