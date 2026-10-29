package com.smartenergy.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "energy_tariffs")
public class EnergyTariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "rate_per_kwh", nullable = false, precision = 10, scale = 4)
    private BigDecimal ratePerKwh = new BigDecimal("0.1500");

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "baseline_target_kwh")
    private Double baselineTargetKwh = 500.0;

    @Column(length = 10, nullable = false)
    private String currency = "$";

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public EnergyTariff() {}

    public EnergyTariff(String name, BigDecimal ratePerKwh, LocalDate effectiveFrom, LocalDate effectiveTo, Double baselineTargetKwh, String currency) {
        this.name = name;
        this.ratePerKwh = ratePerKwh;
        this.effectiveFrom = effectiveFrom != null ? effectiveFrom : LocalDate.now();
        this.effectiveTo = effectiveTo;
        this.baselineTargetKwh = baselineTargetKwh != null ? baselineTargetKwh : 500.0;
        this.currency = currency != null ? currency : "$";
        this.active = true;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.effectiveFrom == null) {
            this.effectiveFrom = LocalDate.now();
        }
    }

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

