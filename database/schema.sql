-- ==============================================================================
-- IoT-Based Automated Energy Monitoring and Management System for Smart Buildings
-- MySQL Database Schema and Seed Data
-- Database: smart_energy_db
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS `smart_energy_db` 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `smart_energy_db`;

-- ------------------------------------------------------------------------------
-- 1. Users Table
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `automation_executions`;
DROP TABLE IF EXISTS `automation_rules`;
DROP TABLE IF EXISTS `energy_tariffs`;
DROP TABLE IF EXISTS `alerts`;
DROP TABLE IF EXISTS `sensor_readings`;
DROP TABLE IF EXISTS `iot_devices`;
DROP TABLE IF EXISTS `zones`;
DROP TABLE IF EXISTS `buildings`;
DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(100) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `role` VARCHAR(30) NOT NULL,
    `active` BIT(1) NOT NULL DEFAULT b'1',
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    INDEX `idx_users_username` (`username`),
    INDEX `idx_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 2. Buildings Table
-- ------------------------------------------------------------------------------
CREATE TABLE `buildings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) NULL,
    `address` VARCHAR(255) NULL,
    `building_type` VARCHAR(50) NOT NULL DEFAULT 'COMMERCIAL',
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 3. Zones Table
-- ------------------------------------------------------------------------------
CREATE TABLE `zones` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) NULL,
    `building_id` BIGINT NOT NULL,
    `area_sqm` DOUBLE NOT NULL DEFAULT 100.0,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_zones_building` FOREIGN KEY (`building_id`) REFERENCES `buildings` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 4. IoT Devices Table
-- ------------------------------------------------------------------------------
CREATE TABLE `iot_devices` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `device_id` VARCHAR(50) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `device_type` VARCHAR(50) NOT NULL DEFAULT 'SMART_METER',
    `zone_id` BIGINT NOT NULL,
    `protocol` VARCHAR(30) NOT NULL DEFAULT 'MQTT',
    `firmware_version` VARCHAR(30) NULL DEFAULT 'v2.0.0',
    `active` BIT(1) NOT NULL DEFAULT b'1',
    `is_online` BIT(1) NOT NULL DEFAULT b'1',
    `simulation_enabled` BIT(1) NOT NULL DEFAULT b'1',
    `last_seen_at` DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6) NULL ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    INDEX `idx_devices_identifier` (`device_id`),
    INDEX `idx_devices_zone` (`zone_id`),
    INDEX `idx_devices_status` (`is_online`, `active`),
    CONSTRAINT `fk_devices_zone` FOREIGN KEY (`zone_id`) REFERENCES `zones` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 5. Sensor Readings Table (Time-Series Energy Telemetry)
-- ------------------------------------------------------------------------------
CREATE TABLE `sensor_readings` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `device_id` BIGINT NOT NULL,
    `zone_id` BIGINT NOT NULL,
    `voltage` DOUBLE NOT NULL,
    `current` DOUBLE NOT NULL,
    `power` DOUBLE NOT NULL,
    `energy_kwh` DOUBLE NOT NULL,
    `power_factor` DOUBLE NOT NULL DEFAULT 0.95,
    `is_simulated` BIT(1) NOT NULL DEFAULT b'1',
    `timestamp` DATETIME(6) NOT NULL,
    `recorded_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    INDEX `idx_readings_device_time` (`device_id`, `timestamp`),
    INDEX `idx_readings_zone_time` (`zone_id`, `timestamp`),
    INDEX `idx_readings_timestamp` (`timestamp`),
    CONSTRAINT `fk_readings_device` FOREIGN KEY (`device_id`) REFERENCES `iot_devices` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_readings_zone` FOREIGN KEY (`zone_id`) REFERENCES `zones` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 6. Alerts Table
-- ------------------------------------------------------------------------------
CREATE TABLE `alerts` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `title` VARCHAR(150) NOT NULL,
    `description` VARCHAR(500) NOT NULL,
    `severity` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    `alert_type` VARCHAR(50) NOT NULL,
    `device_id` BIGINT NULL,
    `zone_id` BIGINT NULL,
    `building_id` BIGINT NULL,
    `is_acknowledged` BIT(1) NOT NULL DEFAULT b'0',
    `acknowledged_by` VARCHAR(100) NULL,
    `acknowledged_at` DATETIME(6) NULL,
    `is_resolved` BIT(1) NOT NULL DEFAULT b'0',
    `resolved_by` VARCHAR(100) NULL,
    `resolved_at` DATETIME(6) NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    INDEX `idx_alerts_severity` (`severity`),
    INDEX `idx_alerts_status` (`is_resolved`, `is_acknowledged`),
    INDEX `idx_alerts_created` (`created_at`),
    CONSTRAINT `fk_alerts_device` FOREIGN KEY (`device_id`) REFERENCES `iot_devices` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_alerts_zone` FOREIGN KEY (`zone_id`) REFERENCES `zones` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_alerts_building` FOREIGN KEY (`building_id`) REFERENCES `buildings` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 7. Energy Tariffs Table
-- ------------------------------------------------------------------------------
CREATE TABLE `energy_tariffs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `rate_per_kwh` DECIMAL(10, 4) NOT NULL,
    `effective_from` DATE NOT NULL,
    `effective_to` DATE NULL,
    `baseline_target_kwh` DOUBLE NOT NULL DEFAULT 500.0,
    `currency` VARCHAR(10) NOT NULL DEFAULT '$',
    `active` BIT(1) NOT NULL DEFAULT b'1',
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 8. Automation Rules Table
-- ------------------------------------------------------------------------------
CREATE TABLE `automation_rules` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(255) NULL,
    `scope` VARCHAR(30) NOT NULL DEFAULT 'ALL',
    `target_id` BIGINT NULL,
    `target_name` VARCHAR(100) NULL,
    `trigger_type` VARCHAR(50) NOT NULL,
    `threshold_value` DOUBLE NOT NULL,
    `action_cooldown_min` INT NOT NULL DEFAULT 15,
    `action_type` VARCHAR(50) NOT NULL,
    `enabled` BIT(1) NOT NULL DEFAULT b'1',
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 9. Automation Executions Table
-- ------------------------------------------------------------------------------
CREATE TABLE `automation_executions` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `rule_id` BIGINT NOT NULL,
    `trigger_event` VARCHAR(100) NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS',
    `execution_details` VARCHAR(500) NULL,
    `triggered_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_exec_rule` FOREIGN KEY (`rule_id`) REFERENCES `automation_rules` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==============================================================================
-- INITIAL SEED DATA
-- ==============================================================================

-- 1. Users (password for all seeded accounts is: admin123 / manager123 / operator123 / viewer123)
-- BCrypt encoded hash: $2a$10$wE1M/o8Gf5m7LgNq/s5wMeKzPskKvZV0sC3gQ6Pq4rJtqYfN4NfS2
INSERT INTO `users` (`id`, `username`, `email`, `password`, `full_name`, `role`, `active`, `created_at`) VALUES
(1, 'admin', 'admin@smartbuilding.io', '$2a$10$G0m.KzO1q8Yx7Y5kL9Bge.0Cvy/6bB7BszW97o3xMvQc2uEfgfRsm', 'System Administrator', 'ROLE_ADMIN', 1, NOW()),
(2, 'manager', 'manager@smartbuilding.io', '$2a$10$G0m.KzO1q8Yx7Y5kL9Bge.0Cvy/6bB7BszW97o3xMvQc2uEfgfRsm', 'Energy Operations Manager', 'ROLE_MANAGER', 1, NOW()),
(3, 'operator', 'operator@smartbuilding.io', '$2a$10$G0m.KzO1q8Yx7Y5kL9Bge.0Cvy/6bB7BszW97o3xMvQc2uEfgfRsm', 'Building Systems Technician', 'ROLE_OPERATOR', 1, NOW()),
(4, 'viewer', 'viewer@smartbuilding.io', '$2a$10$G0m.KzO1q8Yx7Y5kL9Bge.0Cvy/6bB7BszW97o3xMvQc2uEfgfRsm', 'Viva Reviewer / Guest', 'ROLE_VIEWER', 1, NOW());

-- 2. Buildings
INSERT INTO `buildings` (`id`, `name`, `description`, `address`, `building_type`, `created_at`) VALUES
(1, 'Apex Engineering Complex', 'Main campus smart facility with integrated IoT energy metering', 'Academic Zone North, Block 4', 'ACADEMIC', NOW()),
(2, 'EcoTech Operations Center', 'Auxiliary administration and research data center wing', 'Innovation Park, Sector 7', 'COMMERCIAL', NOW());

-- 3. Zones
INSERT INTO `zones` (`id`, `name`, `description`, `building_id`, `area_sqm`, `created_at`) VALUES
(1, 'Data Center & Server Hall', 'High-density server racks and dual-redundant CRAC units', 1, 140.0, NOW()),
(2, 'HVAC & Chiller Plant', 'Central rooftop chillers, air-handling units and circulation pumps', 1, 200.0, NOW()),
(3, 'IoT Research & Robotics Lab', 'Electronics test benches, 3D printing bay and computing stations', 1, 80.0, NOW()),
(4, 'Auditorium & Conference Suites', 'Multi-tier lecture hall with dynamic theatrical lighting and AV', 1, 50.0, NOW()),
(5, 'Operations & Administration', 'Offices, meeting rooms and workspace amenities', 2, 90.0, NOW());

-- 4. IoT Devices
INSERT INTO `iot_devices` (`id`, `device_id`, `name`, `device_type`, `zone_id`, `protocol`, `firmware_version`, `active`, `is_online`, `simulation_enabled`, `last_seen_at`, `created_at`) VALUES
(1, 'ESP32-001', 'CRAC Unit 1 Feeder Monitor', 'ESP32_PZEM004T', 1, 'MQTT', 'v2.1.0-STABLE', 1, 1, 1, NOW(), NOW()),
(2, 'ESP32-002', 'Central Chiller 2 Meter', 'ESP32_PZEM004T', 2, 'MQTT', 'v2.1.0-STABLE', 1, 1, 1, NOW(), NOW()),
(3, 'ESP32-003', 'Robotics Lab Sub-Panel', 'ESP32_ACS712', 3, 'MQTT', 'v1.9.4-PROD', 1, 1, 1, NOW(), NOW()),
(4, 'ESP32-004', 'Auditorium Main Dimmer Bank', 'ESP32_PZEM004T', 4, 'MQTT', 'v2.0.1-STABLE', 1, 1, 1, NOW(), NOW()),
(5, 'ESP32-005', 'Admin Floor Lighting Panel', 'SMART_METER', 5, 'MQTT', 'v1.4.2-PROD', 1, 1, 1, NOW(), NOW());

-- 5. Tariffs
INSERT INTO `energy_tariffs` (`id`, `name`, `rate_per_kwh`, `effective_from`, `baseline_target_kwh`, `currency`, `active`, `created_at`) VALUES
(1, 'Commercial Peak-Demand Tariff', 0.1650, '2026-01-01', 600.0, '$', 1, NOW());

-- 6. Automation Rules
INSERT INTO `automation_rules` (`id`, `name`, `description`, `scope`, `target_id`, `target_name`, `trigger_type`, `threshold_value`, `action_cooldown_min`, `action_type`, `enabled`, `created_at`) VALUES
(1, 'Peak Power Demand Limiter', 'Monitors instantaneous facility demand and initiates load shedding when limit is exceeded', 'ALL', NULL, 'Entire Facility', 'POWER_THRESHOLD_EXCEEDED', 3500.0, 15, 'SIMULATED_LOAD_SHEDDING', 1, NOW()),
(2, 'HVAC Plant Daily Budget Guard', 'Applies eco-setback profile when chiller consumption exceeds daily allocation', 'ZONE', 2, 'HVAC & Chiller Plant', 'DAILY_TARGET_EXCEEDED', 180.0, 30, 'SET_ECO_MODE_SIMULATED', 1, NOW()),
(3, 'Substation Power Factor Vigilance', 'Identifies poor power factor in inductive equipment to eliminate reactive penalty surcharges', 'ALL', NULL, 'Entire Facility', 'ABNORMAL_CONSUMPTION', 2800.0, 20, 'ALERT_ONLY', 1, NOW()),
(4, 'Telemetry Heartbeat Monitor', 'Detects offline field microcontroller transmitters', 'ALL', NULL, 'All Field Nodes', 'DEVICE_OFFLINE', 1.0, 10, 'ALERT_ONLY', 1, NOW());

-- 7. Initial Alerts
INSERT INTO `alerts` (`id`, `title`, `description`, `severity`, `alert_type`, `device_id`, `zone_id`, `building_id`, `is_acknowledged`, `is_resolved`, `created_at`) VALUES
(1, 'Scheduled Filter Maintenance Due on Chiller 2', 'Sensor reports elevated compressor discharge current (12.4 A). Chiller efficiency may be reduced by 8%.', 'MEDIUM', 'ABNORMAL_USAGE', 2, 2, 1, 0, 0, NOW()),
(2, 'Auditorium Stage Lights Active Off-Hours', 'Unscheduled energy usage detected in Conference Zone outside calendar booking hours.', 'LOW', 'HIGH_POWER', 4, 4, 1, 0, 0, NOW());

-- 8. Seed Sample Readings (Recent 10 minutes)
INSERT INTO `sensor_readings` (`device_id`, `zone_id`, `voltage`, `current`, `power`, `energy_kwh`, `power_factor`, `is_simulated`, `timestamp`, `recorded_at`) VALUES
(1, 1, 230.5, 7.8, 1720.0, 0.028, 0.96, 1, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW()),
(2, 2, 229.8, 9.4, 2050.0, 0.034, 0.95, 1, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW()),
(3, 3, 231.2, 3.2, 700.0, 0.011, 0.94, 1, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW()),
(4, 4, 230.1, 1.8, 390.0, 0.006, 0.94, 1, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW()),
(5, 5, 231.0, 2.4, 520.0, 0.008, 0.94, 1, DATE_SUB(NOW(), INTERVAL 5 MINUTE), NOW()),
(1, 1, 230.2, 8.1, 1780.0, 0.029, 0.95, 1, NOW(), NOW()),
(2, 2, 230.0, 10.2, 2230.0, 0.037, 0.95, 1, NOW(), NOW()),
(3, 3, 231.5, 3.5, 760.0, 0.012, 0.94, 1, NOW(), NOW()),
(4, 4, 229.9, 1.9, 410.0, 0.007, 0.94, 1, NOW(), NOW()),
(5, 5, 230.8, 2.5, 545.0, 0.009, 0.95, 1, NOW(), NOW());
