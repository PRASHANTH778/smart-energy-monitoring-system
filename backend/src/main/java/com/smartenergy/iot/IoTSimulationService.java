package com.smartenergy.iot;

import com.smartenergy.dto.TelemetryPayload;
import com.smartenergy.entity.IoTDevice;
import com.smartenergy.repository.IoTDeviceRepository;
import com.smartenergy.service.SensorReadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

/**
 * IoT Simulation Engine for Smart Energy Monitoring System.
 * Simulates real-time telemetry (Voltage, Current, Power, Power Factor)
 * across distributed building smart meters, chillers, and sub-panels.
 */
@Service
public class IoTSimulationService {

    private static final Logger logger = LoggerFactory.getLogger(IoTSimulationService.class);

    private final IoTDeviceRepository deviceRepository;
    private final SensorReadingService readingService;
    private final Random random = new Random();

    @Value("${app.simulation.enabled:true}")
    private boolean simulationEnabled;

    public IoTSimulationService(IoTDeviceRepository deviceRepository, SensorReadingService readingService) {
        this.deviceRepository = deviceRepository;
        this.readingService = readingService;
    }

    public boolean isSimulationEnabled() {
        return simulationEnabled;
    }

    public void setSimulationEnabled(boolean simulationEnabled) {
        this.simulationEnabled = simulationEnabled;
        logger.info("IoT Simulation Engine toggled to: {}", simulationEnabled);
    }

    /**
     * Periodic scheduled telemetry generation
     */
    @Scheduled(fixedDelayString = "${app.simulation.interval-ms:4000}")
    public void runTelemetrySimulation() {
        if (!simulationEnabled) {
            return;
        }

        List<IoTDevice> devices = deviceRepository.findAll();
        if (devices.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        for (IoTDevice device : devices) {
            if (!device.isActive() || !device.isSimulationEnabled()) {
                continue;
            }

            try {
                // Base voltage between 227V and 233V (standard single phase 230V nominal)
                double baseVoltage = 230.0 + (random.nextDouble() * 6.0 - 3.0);
                double baseCurrent;
                double powerFactor;

                String name = device.getName().toLowerCase();
                if (name.contains("hvac") || name.contains("chiller")) {
                    baseCurrent = 8.5 + random.nextDouble() * 4.0; // ~2000W - 2900W
                    powerFactor = 0.88 + random.nextDouble() * 0.05;
                } else if (name.contains("server") || name.contains("ups")) {
                    baseCurrent = 7.0 + random.nextDouble() * 2.5; // ~1600W - 2200W
                    powerFactor = 0.96 + random.nextDouble() * 0.03;
                } else if (name.contains("conference") || name.contains("hall") || name.contains("auditorium")) {
                    baseCurrent = 1.2 + random.nextDouble() * 2.0; // ~270W - 750W
                    powerFactor = 0.92 + random.nextDouble() * 0.04;
                } else if (name.contains("lighting") || name.contains("floor")) {
                    baseCurrent = 2.0 + random.nextDouble() * 2.0; // ~450W - 900W
                    powerFactor = 0.95 + random.nextDouble() * 0.03;
                } else {
                    baseCurrent = 3.0 + random.nextDouble() * 3.5; // ~700W - 1500W
                    powerFactor = 0.93 + random.nextDouble() * 0.04;
                }

                // Occasionally generate a power spike to demonstrate automated alert generation
                if (random.nextInt(30) == 0) {
                    baseCurrent += 6.5; // Trigger spike exceeding 3000W limit
                    logger.info("[IoT-SIM] Injected simulated load spike on device: {}", device.getName());
                }

                double calculatedPower = baseVoltage * baseCurrent * powerFactor;

                TelemetryPayload payload = new TelemetryPayload(
                        device.getDeviceId(),
                        Math.round(baseVoltage * 100.0) / 100.0,
                        Math.round(baseCurrent * 100.0) / 100.0,
                        Math.round(calculatedPower * 10.0) / 10.0,
                        Math.round(powerFactor * 100.0) / 100.0,
                        now
                );

                readingService.processTelemetry(payload, true);
            } catch (Exception ex) {
                logger.error("[IoT-SIM] Simulation error for device {}: {}", device.getDeviceId(), ex.getMessage());
            }
        }
    }

    /**
     * Manually trigger an instantaneous power spike for live presentation or verification
     */
    public boolean triggerSpikeOnDevice(String deviceIdentifier) {
        return deviceRepository.findByDeviceId(deviceIdentifier).map(device -> {
            double spikeVoltage = 232.0;
            double spikeCurrent = 15.0; // High current
            double powerFactor = 0.91;
            double spikePower = spikeVoltage * spikeCurrent * powerFactor; // ~3166W

            TelemetryPayload payload = new TelemetryPayload(
                    device.getDeviceId(),
                    spikeVoltage,
                    spikeCurrent,
                    spikePower,
                    powerFactor,
                    LocalDateTime.now()
            );

            readingService.processTelemetry(payload, true);
            logger.warn("[IoT-SIM] Triggered forced manual spike on device {}", device.getName());
            return true;
        }).orElse(false);
    }
}

