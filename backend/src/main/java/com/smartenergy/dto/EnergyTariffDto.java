package com.smartenergy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class EnergyTariffDto {
    private Long id;

    @NotBlank(message = "Tariff name is required")
    private String name;

    @NotNull(message = "Rate per kWh is required")
    @DecimalMin(value = "0.0001", message = "Rate must be positive")
    private BigDecimal ratePerKwh;

    @NotNull(message = "Effective date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
    private Double baselineTargetKwh = 500.0;
    private String currency = "$";
    private boolean active = true;

    public EnergyTariffDto() {}

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

    public BigDecimal getRatePerKwh() {
        return ratePerKwh;
    }

    public void setRatePerKwh(BigDecimal ratePerKwh) {
        this.ratePerKwh = ratePerKwh;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public Double getBaselineTargetKwh() {
        return baselineTargetKwh;
    }

    public void setBaselineTargetKwh(Double baselineTargetKwh) {
        this.baselineTargetKwh = baselineTargetKwh;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

