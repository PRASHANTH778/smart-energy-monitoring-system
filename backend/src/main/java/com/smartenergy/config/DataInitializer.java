package com.smartenergy.config;

import com.smartenergy.entity.*;
import com.smartenergy.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final BuildingRepository buildingRepository;
    private final ZoneRepository zoneRepository;
    private final IoTDeviceRepository deviceRepository;
    private final SensorReadingRepository readingRepository;
    private final EnergyTariffRepository tariffRepository;
    private final AutomationRuleRepository ruleRepository;
    private final AlertRepository alertRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           BuildingRepository buildingRepository,
                           ZoneRepository zoneRepository,
                           IoTDeviceRepository deviceRepository,
                           SensorReadingRepository readingRepository,
                           EnergyTariffRepository tariffRepository,
                           AutomationRuleRepository ruleRepository,
                           AlertRepository alertRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.buildingRepository = buildingRepository;
        this.zoneRepository = zoneRepository;
        this.deviceRepository = deviceRepository;
        this.readingRepository = readingRepository;
        this.tariffRepository = tariffRepository;
        this.ruleRepository = ruleRepository;
        this.alertRepository = alertRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            userRepository.findByUsername("admin").ifPresent(u -> {
                u.setPassword(passwordEncoder.encode("admin123"));
                userRepository.save(u);
            });
            userRepository.findByUsername("manager").ifPresent(u -> {
                u.setPassword(passwordEncoder.encode("manager123"));
                userRepository.save(u);
            });
            userRepository.findByUsername("operator").ifPresent(u -> {
                u.setPassword(passwordEncoder.encode("operator123"));
                userRepository.save(u);
            });
            userRepository.findByUsername("viewer").ifPresent(u -> {
                u.setPassword(passwordEncoder.encode("viewer123"));
                userRepository.save(u);
            });
            logger.info("Database already seeded. User credentials synchronized.");
            return;
        }

        logger.info("Seeding initial demonstration database for Smart Building Energy System...");

        // 1. Seed Users
        User admin = new User("admin", "admin@smartbuilding.io", passwordEncoder.encode("admin123"), "System Administrator", Role.ROLE_ADMIN);
        User manager = new User("manager", "manager@smartbuilding.io", passwordEncoder.encode("manager123"), "Energy Operations Manager", Role.ROLE_MANAGER);
        User operator = new User("operator", "operator@smartbuilding.io", passwordEncoder.encode("operator123"), "Building Systems Technician", Role.ROLE_OPERATOR);
        User viewer = new User("viewer", "viewer@smartbuilding.io", passwordEncoder.encode("viewer123"), "Viva Reviewer / Guest", Role.ROLE_VIEWER);

        userRepository.saveAll(List.of(admin, manager, operator, viewer));

        // 2. Seed Buildings
        Building b1 = new Building("Apex Engineering Complex", "Main campus smart facility with IoT energy metering", "Academic Zone North, Block 4", "ACADEMIC");
        Building b2 = new Building("EcoTech Operations Center", "Auxiliary admin and research data center wing", "Innovation Park, Sector 7", "COMMERCIAL");
        buildingRepository.saveAll(List.of(b1, b2));

        // 3. Seed Zones
        Zone z1 = new Zone("Data Center & Server Hall", "High-density server racks and dual-redundant CRAC units", b1, 140.0);
        Zone z2 = new Zone("HVAC & Chiller Plant", "Central rooftop chillers, air-handling units and circulation pumps", b1, 200.0);
        Zone z3 = new Zone("IoT Research & Robotics Lab", "Electronics test benches, 3D printing bay and computing stations", b1, 80.0);
        Zone z4 = new Zone("Auditorium & Conference Suites", "Multi-tier lecture hall with dynamic theatrical lighting and AV", b1, 50.0);
        Zone z5 = new Zone("Operations & Administration", "Offices, meeting rooms and workspace amenities", b2, 90.0);
        zoneRepository.saveAll(List.of(z1, z2, z3, z4, z5));

        // 4. Seed IoT Devices
        IoTDevice d1 = new IoTDevice("ESP32-001", "CRAC Unit 1 Feeder Monitor", "ESP32_PZEM004T", z1, "MQTT", "v2.1.0-STABLE", true);
        IoTDevice d2 = new IoTDevice("ESP32-002", "Central Chiller 2 Meter", "ESP32_PZEM004T", z2, "MQTT", "v2.1.0-STABLE", true);
        IoTDevice d3 = new IoTDevice("ESP32-003", "Robotics Lab Sub-Panel", "ESP32_ACS712", z3, "MQTT", "v1.9.4-PROD", true);
        IoTDevice d4 = new IoTDevice("ESP32-004", "Auditorium Main Dimmer Bank", "ESP32_PZEM004T", z4, "MQTT", "v2.0.1-STABLE", true);
        IoTDevice d5 = new IoTDevice("ESP32-005", "Admin Floor Lighting Panel", "SMART_METER", z5, "MQTT", "v1.4.2-PROD", true);

        deviceRepository.saveAll(List.of(d1, d2, d3, d4, d5));

        // 5. Seed Tariff
        EnergyTariff tariff = new EnergyTariff("Commercial Peak-Demand Tariff", new BigDecimal("0.1650"), LocalDate.of(2026, 1, 1), null, 600.0, "$");
        tariffRepository.save(tariff);

        // 6. Seed Automation Rules
        AutomationRule rule1 = new AutomationRule(
                "Peak Power Demand Limiter",
                "Monitors building instantaneous power and initiates simulated load curtailment when demand peaks",
                "ALL", null, "Entire Facility",
                "POWER_THRESHOLD_EXCEEDED", 3500.0, 15, "SIMULATED_LOAD_SHEDDING"
        );

        AutomationRule rule2 = new AutomationRule(
                "HVAC Plant Daily Budget Guard",
                "Alerts facility operators and applies eco-setback profile when chiller consumption exceeds 180 kWh",
                "ZONE", z2.getId(), z2.getName(),
                "DAILY_TARGET_EXCEEDED", 180.0, 30, "SET_ECO_MODE_SIMULATED"
        );

        AutomationRule rule3 = new AutomationRule(
                "Substation Power Factor Vigilance",
                "Identifies uncorrected reactive loads in research labs to prevent utility penalty surcharges",
                "ALL", null, "Entire Facility",
                "ABNORMAL_CONSUMPTION", 2800.0, 20, "ALERT_ONLY"
        );

        AutomationRule rule4 = new AutomationRule(
                "Telemetry Heartbeat Monitor",
                "Monitors IoT field nodes and notifies engineers if any device drops offline",
                "ALL", null, "All Field Nodes",
                "DEVICE_OFFLINE", 1.0, 10, "ALERT_ONLY"
        );

        ruleRepository.saveAll(List.of(rule1, rule2, rule3, rule4));

        // 7. Seed Sample Initial Alerts
        Alert alert1 = new Alert(
                "Scheduled Filter Maintenance Due on Chiller 2",
                "Sensor reports elevated compressor discharge current (12.4 A). Chiller efficiency may be reduced by 8%.",
                "MEDIUM", "ABNORMAL_USAGE", d2, z2, b1
        );
        Alert alert2 = new Alert(
                "Auditorium Stage Lights Active Off-Hours",
                "Unscheduled energy usage detected in Conference Zone outside calendar booking hours.",
                "LOW", "HIGH_POWER", d4, z4, b1
        );
        alertRepository.saveAll(List.of(alert1, alert2));

        // 8. Seed Historical Sensor Readings for the last 48 hours to ensure charts have rich data
        seedHistoricalReadings(List.of(d1, d2, d3, d4, d5));

        logger.info("Demonstration database successfully seeded with users, buildings, zones, devices, and telemetry history!");
    }

    private void seedHistoricalReadings(List<IoTDevice> devices) {
        List<SensorReading> readings = new ArrayList<>();
        Random random = new Random(42); // deterministic seed for realistic curves
        LocalDateTime now = LocalDateTime.now();

        for (int hourOffset = 48; hourOffset >= 1; hourOffset--) {
            LocalDateTime sampleTime = now.minusHours(hourOffset);
            int hourOfDay = sampleTime.getHour();

            // Daily load shape: lower at night (0-6), higher during working hours (8-18)
            double activityFactor = (hourOfDay >= 8 && hourOfDay <= 18) ? 1.4 : 0.6;

            for (IoTDevice dev : devices) {
                double baseVolts = 228.0 + random.nextDouble() * 5.0;
                double baseAmps;
                double powerFactor = 0.92 + random.nextDouble() * 0.06;

                if (dev.getDeviceId().equals("ESP32-001")) {
                    baseAmps = (7.5 + random.nextDouble() * 2.0); // Server room steady 24/7
                } else if (dev.getDeviceId().equals("ESP32-002")) {
                    baseAmps = (8.0 + random.nextDouble() * 4.0) * activityFactor; // HVAC swings with day
                } else if (dev.getDeviceId().equals("ESP32-003")) {
                    baseAmps = (3.0 + random.nextDouble() * 3.5) * activityFactor;
                } else {
                    baseAmps = (1.5 + random.nextDouble() * 2.0) * activityFactor;
                }

                double powerWatts = baseVolts * baseAmps * powerFactor;
                // Trapezoidal energy for 1-hour interval: E = P * 1 hr / 1000
                double energyKwh = (powerWatts * 1.0) / 1000.0;

                SensorReading reading = new SensorReading(
                        dev,
                        dev.getZone(),
                        Math.round(baseVolts * 10.0) / 10.0,
                        Math.round(baseAmps * 100.0) / 100.0,
                        Math.round(powerWatts * 10.0) / 10.0,
                        Math.round(energyKwh * 1000.0) / 1000.0,
                        Math.round(powerFactor * 100.0) / 100.0,
                        true,
                        sampleTime
                );
                readings.add(reading);
            }
        }

        // Add 10 live recent readings in the past 10 minutes
        for (int minOffset = 10; minOffset >= 0; minOffset--) {
            LocalDateTime sampleTime = now.minusMinutes(minOffset);
            for (IoTDevice dev : devices) {
                double baseVolts = 230.0 + random.nextDouble() * 2.5;
                double baseAmps = 5.0 + random.nextDouble() * 3.0;
                double powerFactor = 0.95;
                double powerWatts = baseVolts * baseAmps * powerFactor;
                double energyKwh = (powerWatts * (1.0 / 60.0)) / 1000.0; // 1 min slice

                SensorReading r = new SensorReading(
                        dev,
                        dev.getZone(),
                        Math.round(baseVolts * 10.0) / 10.0,
                        Math.round(baseAmps * 100.0) / 100.0,
                        Math.round(powerWatts * 10.0) / 10.0,
                        Math.round(energyKwh * 1000.0) / 1000.0,
                        0.95,
                        true,
                        sampleTime
                );
                readings.add(r);
            }
        }

        readingRepository.saveAll(readings);

        // Update devices last seen
        for (IoTDevice dev : devices) {
            dev.setLastSeenAt(now);
            dev.setOnline(true);
            deviceRepository.save(dev);
        }
    }
}

