package com.smartenergy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings", indexes = {
    @Index(name = "idx_reading_device_time", columnList = "device_id, timestamp"),
    @Index(name = "idx_reading_zone_time", columnList = "zone_id, timestamp"),
    @Index(name = "idx_reading_timestamp", columnList = "timestamp")
})
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "device_id", nullable = false)
    private IoTDevice device;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @Column(nullable = false)
    private Double voltage; // in Volts

    @Column(name = "current", nullable = false)
    private Double current; // in Amperes

    @Column(name = "power", nullable = false)
    private Double power; // Active power in Watts

    @Column(name = "energy_kwh", nullable = false)
    private Double energyKwh; // Calculated / integrated Energy in kWh

    @Column(name = "power_factor", nullable = false)
    private Double powerFactor = 0.95;

    @Column(name = "is_simulated", nullable = false)
    private boolean isSimulated = false;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;

    public SensorReading() {}

    public SensorReading(IoTDevice device, Zone zone, Double voltage, Double current, Double power, Double energyKwh, Double powerFactor, boolean isSimulated, LocalDateTime timestamp) {
        this.device = device;
        this.zone = zone;
        this.voltage = voltage;
        this.current = current;
        this.power = power;
        this.energyKwh = energyKwh;
        this.powerFactor = powerFactor != null ? powerFactor : 0.95;
        this.isSimulated = isSimulated;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.recordedAt == null) {
            this.recordedAt = LocalDateTime.now();
        }
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public IoTDevice getDevice() {
        return device;
    }

    public void setDevice(IoTDevice device) {
        this.device = device;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
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

    public Double getEnergyKwh() {
        return energyKwh;
    }

    public void setEnergyKwh(Double energyKwh) {
        this.energyKwh = energyKwh;
    }

    public Double getPowerFactor() {
        return powerFactor;
    }

    public void setPowerFactor(Double powerFactor) {
        this.powerFactor = powerFactor;
    }

    public boolean isSimulated() {
        return isSimulated;
    }

    public void setSimulated(boolean simulated) {
        isSimulated = simulated;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}

