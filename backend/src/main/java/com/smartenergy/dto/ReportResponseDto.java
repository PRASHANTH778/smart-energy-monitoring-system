package com.smartenergy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportResponseDto {
    private String reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalEnergyKwh;
    private BigDecimal totalEstimatedCost;
    private BigDecimal averageTariffRate;
    private int targetViolationsCount;
    private int totalAlertsCount;

    private List<ReportItem> items = new ArrayList<>();

    public ReportResponseDto() {}

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getTotalEnergyKwh() {
        return totalEnergyKwh;
    }

    public void setTotalEnergyKwh(Double totalEnergyKwh) {
        this.totalEnergyKwh = totalEnergyKwh;
    }

    public BigDecimal getTotalEstimatedCost() {
        return totalEstimatedCost;
    }

    public void setTotalEstimatedCost(BigDecimal totalEstimatedCost) {
        this.totalEstimatedCost = totalEstimatedCost;
    }

    public BigDecimal getAverageTariffRate() {
        return averageTariffRate;
    }

    public void setAverageTariffRate(BigDecimal averageTariffRate) {
        this.averageTariffRate = averageTariffRate;
    }

    public int getTargetViolationsCount() {
        return targetViolationsCount;
    }

    public void setTargetViolationsCount(int targetViolationsCount) {
        this.targetViolationsCount = targetViolationsCount;
    }

    public int getTotalAlertsCount() {
        return totalAlertsCount;
    }

    public void setTotalAlertsCount(int totalAlertsCount) {
        this.totalAlertsCount = totalAlertsCount;
    }

    public List<ReportItem> getItems() {
        return items;
    }

    public void setItems(List<ReportItem> items) {
        this.items = items;
    }

    public static class ReportItem {
        private String periodOrEntity;
        private Double energyKwh;
        private Double avgPowerWatts;
        private BigDecimal cost;
        private Double targetKwh;
        private String status; // NORMAL, TARGET_EXCEEDED

        public ReportItem() {}

        public ReportItem(String periodOrEntity, Double energyKwh, Double avgPowerWatts, BigDecimal cost, Double targetKwh, String status) {
            this.periodOrEntity = periodOrEntity;
            this.energyKwh = energyKwh;
            this.avgPowerWatts = avgPowerWatts;
            this.cost = cost;
            this.targetKwh = targetKwh;
            this.status = status;
        }

        public String getPeriodOrEntity() {
            return periodOrEntity;
        }

        public void setPeriodOrEntity(String periodOrEntity) {
            this.periodOrEntity = periodOrEntity;
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

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

