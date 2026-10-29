package com.smartenergy.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EnergyAnalyticsDto {
    private Double totalEnergyKwh;
    private BigDecimal totalCost;
    private Double avgPowerWatts;
    private Double avgPowerFactor;
    private BigDecimal baselineSavings;

    private List<TimePointData> hourlyTrends = new ArrayList<>();
    private List<TimePointData> dailyTrends = new ArrayList<>();
    private List<TimePointData> weeklyTrends = new ArrayList<>();
    private List<TimePointData> monthlyTrends = new ArrayList<>();
    private List<ZoneAnalyticsData> zoneBreakdown = new ArrayList<>();
    private List<DeviceAnalyticsData> deviceBreakdown = new ArrayList<>();

    public EnergyAnalyticsDto() {}

    public Double getTotalEnergyKwh() {
        return totalEnergyKwh;
    }

    public void setTotalEnergyKwh(Double totalEnergyKwh) {
        this.totalEnergyKwh = totalEnergyKwh;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public Double getAvgPowerWatts() {
        return avgPowerWatts;
    }

    public void setAvgPowerWatts(Double avgPowerWatts) {
        this.avgPowerWatts = avgPowerWatts;
    }

    public Double getAvgPowerFactor() {
        return avgPowerFactor;
    }

    public void setAvgPowerFactor(Double avgPowerFactor) {
        this.avgPowerFactor = avgPowerFactor;
    }

    public BigDecimal getBaselineSavings() {
        return baselineSavings;
    }

    public void setBaselineSavings(BigDecimal baselineSavings) {
        this.baselineSavings = baselineSavings;
    }

    public List<TimePointData> getHourlyTrends() {
        return hourlyTrends;
    }

    public void setHourlyTrends(List<TimePointData> hourlyTrends) {
        this.hourlyTrends = hourlyTrends;
    }

    public List<TimePointData> getDailyTrends() {
        return dailyTrends;
    }

    public void setDailyTrends(List<TimePointData> dailyTrends) {
        this.dailyTrends = dailyTrends;
    }

    public List<TimePointData> getWeeklyTrends() {
        return weeklyTrends;
    }

    public void setWeeklyTrends(List<TimePointData> weeklyTrends) {
        this.weeklyTrends = weeklyTrends;
    }

    public List<TimePointData> getMonthlyTrends() {
        return monthlyTrends;
    }

    public void setMonthlyTrends(List<TimePointData> monthlyTrends) {
        this.monthlyTrends = monthlyTrends;
    }

    public List<ZoneAnalyticsData> getZoneBreakdown() {
        return zoneBreakdown;
    }

    public void setZoneBreakdown(List<ZoneAnalyticsData> zoneBreakdown) {
        this.zoneBreakdown = zoneBreakdown;
    }

    public List<DeviceAnalyticsData> getDeviceBreakdown() {
        return deviceBreakdown;
    }

    public void setDeviceBreakdown(List<DeviceAnalyticsData> deviceBreakdown) {
        this.deviceBreakdown = deviceBreakdown;
    }

    public static class TimePointData {
        private String label;
        private Double energyKwh;
        private Double avgPowerWatts;
        private BigDecimal cost;
        private Double targetKwh;

        public TimePointData() {}

        public TimePointData(String label, Double energyKwh, Double avgPowerWatts, BigDecimal cost, Double targetKwh) {
            this.label = label;
            this.energyKwh = energyKwh;
            this.avgPowerWatts = avgPowerWatts;
            this.cost = cost;
            this.targetKwh = targetKwh;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public Double getEnergyKwh() {
            return energyKwh;
        }

        public void setEnergyKwh(Double energyKwh) {
            this.energyKwh = energyKwh;
        }

        public Double getAvgPowerWatts() {
            return avgPowerWatts;
        }

        public void setAvgPowerWatts(Double avgPowerWatts) {
            this.avgPowerWatts = avgPowerWatts;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        public Double getTargetKwh() {
            return targetKwh;
        }

        public void setTargetKwh(Double targetKwh) {
            this.targetKwh = targetKwh;
        }
    }

    public static class ZoneAnalyticsData {
        private String zoneName;
        private Double energyKwh;
        private Double percentage;
        private BigDecimal cost;
        private Double targetKwh;

        public ZoneAnalyticsData() {}

        public ZoneAnalyticsData(String zoneName, Double energyKwh, Double percentage, BigDecimal cost, Double targetKwh) {
            this.zoneName = zoneName;
            this.energyKwh = energyKwh;
            this.percentage = percentage;
            this.cost = cost;
            this.targetKwh = targetKwh;
        }

        public String getZoneName() {
            return zoneName;
        }

        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }

        public Double getEnergyKwh() {
            return energyKwh;
        }

        public void setEnergyKwh(Double energyKwh) {
            this.energyKwh = energyKwh;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        public Double getTargetKwh() {
            return targetKwh;
        }

        public void setTargetKwh(Double targetKwh) {
            this.targetKwh = targetKwh;
        }
    }

    public static class DeviceAnalyticsData {
        private String deviceName;
        private String deviceId;
        private Double energyKwh;
        private Double percentage;

        public DeviceAnalyticsData() {}

        public DeviceAnalyticsData(String deviceName, String deviceId, Double energyKwh, Double percentage) {
            this.deviceName = deviceName;
            this.deviceId = deviceId;
            this.energyKwh = energyKwh;
            this.percentage = percentage;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public Double getEnergyKwh() {
            return energyKwh;
        }

        public void setEnergyKwh(Double energyKwh) {
            this.energyKwh = energyKwh;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }
}

