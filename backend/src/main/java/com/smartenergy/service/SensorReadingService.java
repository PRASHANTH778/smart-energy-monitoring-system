package com.smartenergy.service;

import com.smartenergy.dto.SensorReadingDto;
import com.smartenergy.dto.TelemetryPayload;
import com.smartenergy.exception.BadRequestException;
import com.smartenergy.exception.ResourceNotFoundException;
import com.smartenergy.entity.IoTDevice;
import com.smartenergy.entity.SensorReading;
import com.smartenergy.repository.IoTDeviceRepository;
import com.smartenergy.repository.SensorReadingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SensorReadingService {

    private static final Logger logger = LoggerFactory.getLogger(SensorReadingService.class);

    private final SensorReadingRepository readingRepository;
    private final IoTDeviceRepository deviceRepository;
    private final EnergyCalculationService calculationService;
    private final AlertService alertService;

    public SensorReadingService(SensorReadingRepository readingRepository,
                                IoTDeviceRepository deviceRepository,
                                EnergyCalculationService calculationService,
                                AlertService alertService) {
        this.readingRepository = readingRepository;
        this.deviceRepository = deviceRepository;
        this.calculationService = calculationService;
        this.alertService = alertService;
    }

    /**
     * Ingests telemetry reading from MQTT or REST API endpoint
     */
    @Transactional
    public SensorReadingDto processTelemetry(TelemetryPayload payload, boolean isSimulated) {
        if (payload.getDeviceId() == null || payload.getDeviceId().isBlank()) {
            throw new BadRequestException("Device ID is required in telemetry payload");
        }

        IoTDevice device = deviceRepository.findByDeviceId(payload.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Unknown IoT Device: " + payload.getDeviceId()));

        if (!device.isActive()) {
            throw new BadRequestException("Device is disabled/inactive: " + payload.getDeviceId());
        }

        LocalDateTime timestamp = payload.getTimestamp() != null ? payload.getTimestamp() : LocalDateTime.now();
        Double voltage = payload.getVoltage() != null ? payload.getVoltage() : 230.0;
        Double current = payload.getCurrent() != null ? payload.getCurrent() : 0.0;
        Double powerFactor = payload.getPowerFactor() != null ? payload.getPowerFactor() : 0.95;

        // Calculate power if not provided: P = V * I * PF
        Double power = payload.getPower();
        if (power == null || power <= 0.0) {
            power = voltage * current * powerFactor;
        }

        // Fetch previous reading for trapezoidal numerical integration
        Optional<SensorReading> previousReadingOpt = readingRepository.findTopByDeviceIdOrderByTimestampDesc(device.getId());

        Double intervalEnergyKwh;
        if (previousReadingOpt.isPresent()) {
            SensorReading prev = previousReadingOpt.get();
            intervalEnergyKwh = calculationService.calculateIntervalEnergy(
                    prev.getPower(), power, prev.getTimestamp(), timestamp
            );
        } else {
            // First reading fallback: standard 5 second equivalent
            intervalEnergyKwh = calculationService.calculateIntervalEnergy(
                    power, power, null, timestamp
            );
        }

        SensorReading reading = new SensorReading(
                device,
                device.getZone(),
                Math.round(voltage * 100.0) / 100.0,
                Math.round(current * 100.0) / 100.0,
                Math.round(power * 10.0) / 10.0,
                intervalEnergyKwh,
                Math.round(powerFactor * 100.0) / 100.0,
                isSimulated,
                timestamp
        );

        SensorReading saved = readingRepository.save(reading);

        // Update device connection status and heartbeat
        device.setLastSeenAt(timestamp);
        device.setOnline(true);
        deviceRepository.save(device);

        // Real-time anomaly checks
        performThresholdAlertChecks(reading, device);

        return mapToDto(saved);
    }

    private void performThresholdAlertChecks(SensorReading reading, IoTDevice device) {
        // 1. Overpower alert (> 3000 W)
        if (reading.getPower() > 3000.0) {
            alertService.triggerAlertWithCooldown(
                    "High Power Surge on " + device.getName(),
                    String.format("Measured power %.1f W exceeded safe threshold (3000 W) at %s",
                            reading.getPower(), device.getZone().getName()),
                    "HIGH",
                    "HIGH_POWER",
                    device,
                    device.getZone(),
                    device.getZone().getBuilding(),
                    10
            );
        }

        // 2. Voltage fluctuation (< 200V or > 250V)
        if (reading.getVoltage() < 200.0 || reading.getVoltage() > 255.0) {
            alertService.triggerAlertWithCooldown(
                    "Abnormal Line Voltage on " + device.getName(),
                    String.format("Voltage %.1f V is outside nominal operating boundaries (200V - 250V)",
                            reading.getVoltage()),
                    "MEDIUM",
                    "ABNORMAL_USAGE",
                    device,
                    device.getZone(),
                    device.getZone().getBuilding(),
                    15
            );
        }

        // 3. Low power factor (< 0.82)
        if (reading.getPowerFactor() < 0.82) {
            alertService.triggerAlertWithCooldown(
                    "Low Power Factor Penalty Alert",
                    String.format("Power factor dropped to %.2f on device %s. Reactive power penalties may apply.",
                            reading.getPowerFactor(), device.getName()),
                    "LOW",
                    "ABNORMAL_USAGE",
                    device,
                    device.getZone(),
                    device.getZone().getBuilding(),
                    30
            );
        }
    }

    public Page<SensorReadingDto> getReadings(Pageable pageable) {
        return readingRepository.findByOrderByTimestampDesc(pageable)
                .map(this::mapToDto);
    }

    public List<SensorReadingDto> getRecentReadings() {
        return readingRepository.findTop20ByOrderByTimestampDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public SensorReadingDto mapToDto(SensorReading reading) {
        SensorReadingDto dto = new SensorReadingDto();
        dto.setId(reading.getId());
        dto.setVoltage(reading.getVoltage());
        dto.setCurrent(reading.getCurrent());
        dto.setPower(reading.getPower());
        dto.setEnergyKwh(reading.getEnergyKwh());
        dto.setPowerFactor(reading.getPowerFactor());
        dto.setSimulated(reading.isSimulated());
        dto.setTimestamp(reading.getTimestamp());
        dto.setRecordedAt(reading.getRecordedAt());

        if (reading.getDevice() != null) {
            dto.setDeviceId(reading.getDevice().getId());
            dto.setDeviceIdentifier(reading.getDevice().getDeviceId());
            dto.setDeviceName(reading.getDevice().getName());
        }

        if (reading.getZone() != null) {
            dto.setZoneId(reading.getZone().getId());
            dto.setZoneName(reading.getZone().getName());
            if (reading.getZone().getBuilding() != null) {
                dto.setBuildingId(reading.getZone().getBuilding().getId());
                dto.setBuildingName(reading.getZone().getBuilding().getName());
            }
        }

        return dto;
    }
}

