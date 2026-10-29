package com.smartenergy.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardSummaryDto {
    private Double currentPowerWatts;
    private Double energyTodayKwh;
    private BigDecimal estimatedCostToday;
    private Double energyThisMonthKwh;
    private BigDecimal estimatedCostThisMonth;
    private BigDecimal estimatedSavingsMonth;
    private long connectedDevicesCount;
    private long offlineDevicesCount;
    private long activeAlertsCount;
    private Double averagePowerFactor;
    private boolean simulationModeActive;
    private String currency = "$";

    private List<SensorReadingDto> recentReadings = new ArrayList<>();
    private List<AlertDto> recentAlerts = new ArrayList<>();
    private List<ZoneConsumptionDto> topConsumingZones = new ArrayList<>();
    private List<String> energySavingRecommendations = new ArrayList<>();
    private List<AutomationExecutionDto> recentAutomationActions = new ArrayList<>();

    public DashboardSummaryDto() {}

    public Double getCurrentPowerWatts() {
        return currentPowerWatts;
    }

    public void setCurrentPowerWatts(Double currentPowerWatts) {
        this.currentPowerWatts = currentPowerWatts;
    }

    public Double getEnergyTodayKwh() {
        return energyTodayKwh;
    }

    public void setEnergyTodayKwh(Double energyTodayKwh) {
        this.energyTodayKwh = energyTodayKwh;
    }

    public BigDecimal getEstimatedCostToday() {
        return estimatedCostToday;
    }

    public void setEstimatedCostToday(BigDecimal estimatedCostToday) {
        this.estimatedCostToday = estimatedCostToday;
    }

    public Double getEnergyThisMonthKwh() {
        return energyThisMonthKwh;
    }

    public void setEnergyThisMonthKwh(Double energyThisMonthKwh) {
        this.energyThisMonthKwh = energyThisMonthKwh;
    }

    public BigDecimal getEstimatedCostThisMonth() {
        return estimatedCostThisMonth;
    }

    public void setEstimatedCostThisMonth(BigDecimal estimatedCostThisMonth) {
        this.estimatedCostThisMonth = estimatedCostThisMonth;
    }

    public BigDecimal getEstimatedSavingsMonth() {
        return estimatedSavingsMonth;
    }

    public void setEstimatedSavingsMonth(BigDecimal estimatedSavingsMonth) {
        this.estimatedSavingsMonth = estimatedSavingsMonth;
    }

    public long getConnectedDevicesCount() {
        return connectedDevicesCount;
    }

    public void setConnectedDevicesCount(long connectedDevicesCount) {
        this.connectedDevicesCount = connectedDevicesCount;
    }

    public long getOfflineDevicesCount() {
        return offlineDevicesCount;
    }

    public void setOfflineDevicesCount(long offlineDevicesCount) {
        this.offlineDevicesCount = offlineDevicesCount;
    }

    public long getActiveAlertsCount() {
        return activeAlertsCount;
    }

    public void setActiveAlertsCount(long activeAlertsCount) {
        this.activeAlertsCount = activeAlertsCount;
    }

    public Double getAveragePowerFactor() {
        return averagePowerFactor;
    }

    public void setAveragePowerFactor(Double averagePowerFactor) {
        this.averagePowerFactor = averagePowerFactor;
    }

    public boolean isSimulationModeActive() {
        return simulationModeActive;
    }

    public void setSimulationModeActive(boolean simulationModeActive) {
        this.simulationModeActive = simulationModeActive;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<SensorReadingDto> getRecentReadings() {
        return recentReadings;
    }

    public void setRecentReadings(List<SensorReadingDto> recentReadings) {
        this.recentReadings = recentReadings;
    }

    public List<AlertDto> getRecentAlerts() {
        return recentAlerts;
    }

    public void setRecentAlerts(List<AlertDto> recentAlerts) {
        this.recentAlerts = recentAlerts;
    }

    public List<ZoneConsumptionDto> getTopConsumingZones() {
        return topConsumingZones;
    }

    public void setTopConsumingZones(List<ZoneConsumptionDto> topConsumingZones) {
        this.topConsumingZones = topConsumingZones;
    }

    public List<String> getEnergySavingRecommendations() {
        return energySavingRecommendations;
    }

    public void setEnergySavingRecommendations(List<String> energySavingRecommendations) {
        this.energySavingRecommendations = energySavingRecommendations;
    }

    public List<AutomationExecutionDto> getRecentAutomationActions() {
        return recentAutomationActions;
    }

    public void setRecentAutomationActions(List<AutomationExecutionDto> recentAutomationActions) {
        this.recentAutomationActions = recentAutomationActions;
    }

    public static class ZoneConsumptionDto {
        private Long zoneId;
        private String zoneName;
        private Double energyKwh;
        private Double targetKwh;
        private BigDecimal cost;
        private Double percentageOfTotal;

        public ZoneConsumptionDto() {}

        public ZoneConsumptionDto(Long zoneId, String zoneName, Double energyKwh, Double targetKwh, BigDecimal cost, Double percentageOfTotal) {
            this.zoneId = zoneId;
            this.zoneName = zoneName;
            this.energyKwh = energyKwh;
            this.targetKwh = targetKwh;
            this.cost = cost;
            this.percentageOfTotal = percentageOfTotal;
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

        public Double getEnergyKwh() {
            return energyKwh;
        }

        public void setEnergyKwh(Double energyKwh) {
            this.energyKwh = energyKwh;
        }

        public Double getTargetKwh() {
            return targetKwh;
        }

        public void setTargetKwh(Double targetKwh) {
            this.targetKwh = targetKwh;
        }

        public BigDecimal getCost() {
            return cost;
        }

        public void setCost(BigDecimal cost) {
            this.cost = cost;
        }

        public Double getPercentageOfTotal() {
            return percentageOfTotal;
        }

        public void setPercentageOfTotal(Double percentageOfTotal) {
            this.percentageOfTotal = percentageOfTotal;
        }
    }
}

