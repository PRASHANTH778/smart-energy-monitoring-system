package com.smartenergy.service;

import com.smartenergy.dto.AutomationExecutionDto;
import com.smartenergy.dto.AutomationRuleDto;
import com.smartenergy.exception.ResourceNotFoundException;
import com.smartenergy.entity.*;
import com.smartenergy.repository.AutomationExecutionRepository;
import com.smartenergy.repository.AutomationRuleRepository;
import com.smartenergy.repository.IoTDeviceRepository;
import com.smartenergy.repository.SensorReadingRepository;
import com.smartenergy.repository.ZoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AutomationService {

    private static final Logger logger = LoggerFactory.getLogger(AutomationService.class);

    private final AutomationRuleRepository ruleRepository;
    private final AutomationExecutionRepository executionRepository;
    private final SensorReadingRepository readingRepository;
    private final IoTDeviceRepository deviceRepository;
    private final ZoneRepository zoneRepository;
    private final AlertService alertService;

    public AutomationService(AutomationRuleRepository ruleRepository,
                             AutomationExecutionRepository executionRepository,
                             SensorReadingRepository readingRepository,
                             IoTDeviceRepository deviceRepository,
                             ZoneRepository zoneRepository,
                             AlertService alertService) {
        this.ruleRepository = ruleRepository;
        this.executionRepository = executionRepository;
        this.readingRepository = readingRepository;
        this.deviceRepository = deviceRepository;
        this.zoneRepository = zoneRepository;
        this.alertService = alertService;
    }

    public List<AutomationRuleDto> getAllRules() {
        return ruleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public AutomationRuleDto getRuleById(Long id) {
        AutomationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automation rule not found with id: " + id));
        return mapToDto(rule);
    }

    @Transactional
    public AutomationRuleDto createRule(AutomationRuleDto dto) {
        AutomationRule rule = new AutomationRule(
                dto.getName(),
                dto.getDescription(),
                dto.getTargetType(),
                dto.getTargetId(),
                dto.getTargetName(),
                dto.getConditionType(),
                dto.getThresholdValue(),
                dto.getCooldownMinutes(),
                dto.getActionType()
        );
        rule.setEnabled(dto.isEnabled());
        if (dto.getEvaluationIntervalSeconds() != null) {
            rule.setEvaluationIntervalSeconds(dto.getEvaluationIntervalSeconds());
        }

        return mapToDto(ruleRepository.save(rule));
    }

    @Transactional
    public AutomationRuleDto updateRule(Long id, AutomationRuleDto dto) {
        AutomationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automation rule not found with id: " + id));

        rule.setName(dto.getName());
        rule.setDescription(dto.getDescription());
        rule.setTargetType(dto.getTargetType());
        rule.setTargetId(dto.getTargetId());
        rule.setTargetName(dto.getTargetName());
        rule.setConditionType(dto.getConditionType());
        rule.setThresholdValue(dto.getThresholdValue());
        rule.setActionType(dto.getActionType());
        rule.setEnabled(dto.isEnabled());
        if (dto.getCooldownMinutes() != null) {
            rule.setCooldownMinutes(dto.getCooldownMinutes());
        }
        if (dto.getEvaluationIntervalSeconds() != null) {
            rule.setEvaluationIntervalSeconds(dto.getEvaluationIntervalSeconds());
        }

        return mapToDto(ruleRepository.save(rule));
    }

    @Transactional
    public void deleteRule(Long id) {
        AutomationRule rule = ruleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Automation rule not found with id: " + id));
        ruleRepository.delete(rule);
    }

    public List<AutomationExecutionDto> getRecentExecutions() {
        return executionRepository.findTop20ByOrderByExecutedAtDesc().stream()
                .map(this::mapExecutionToDto)
                .collect(Collectors.toList());
    }

    /**
     * Evaluates all enabled automation rules periodically every 30 seconds
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void evaluateRules() {
        List<AutomationRule> rules = ruleRepository.findByEnabledTrue();

        for (AutomationRule rule : rules) {
            try {
                evaluateSingleRule(rule);
            } catch (Exception ex) {
                logger.error("Error evaluating automation rule #{}: {}", rule.getId(), ex.getMessage());
            }
        }
    }

    private void evaluateSingleRule(AutomationRule rule) {
        rule.setLastEvaluatedAt(LocalDateTime.now());

        // Check cooldown
        if (rule.getLastTriggeredAt() != null) {
            LocalDateTime cooldownEnd = rule.getLastTriggeredAt().plusMinutes(rule.getCooldownMinutes());
            if (LocalDateTime.now().isBefore(cooldownEnd)) {
                ruleRepository.save(rule);
                return;
            }
        }

        boolean conditionTriggered = false;
        String reason = "";
        IoTDevice targetDevice = null;
        Zone targetZone = null;
        Building targetBuilding = null;

        switch (rule.getConditionType()) {
            case "POWER_THRESHOLD_EXCEEDED":
                if ("DEVICE".equalsIgnoreCase(rule.getTargetType()) && rule.getTargetId() != null) {
                    Optional<IoTDevice> devOpt = deviceRepository.findById(rule.getTargetId());
                    if (devOpt.isPresent()) {
                        targetDevice = devOpt.get();
                        targetZone = targetDevice.getZone();
                        targetBuilding = targetZone != null ? targetZone.getBuilding() : null;
                        Optional<SensorReading> latest = readingRepository.findTopByDeviceIdOrderByTimestampDesc(targetDevice.getId());
                        if (latest.isPresent() && latest.get().getPower() > rule.getThresholdValue()) {
                            conditionTriggered = true;
                            reason = String.format("Device %s power (%.1f W) exceeded rule threshold (%.1f W)",
                                    targetDevice.getName(), latest.get().getPower(), rule.getThresholdValue());
                        }
                    }
                } else {
                    // Evaluate latest reading system-wide
                    Optional<SensorReading> latestOverall = readingRepository.findTopByOrderByTimestampDesc();
                    if (latestOverall.isPresent() && latestOverall.get().getPower() > rule.getThresholdValue()) {
                        conditionTriggered = true;
                        targetDevice = latestOverall.get().getDevice();
                        targetZone = latestOverall.get().getZone();
                        targetBuilding = targetZone != null ? targetZone.getBuilding() : null;
                        reason = String.format("Current active power (%.1f W) exceeded threshold (%.1f W)",
                                latestOverall.get().getPower(), rule.getThresholdValue());
                    }
                }
                break;

            case "DAILY_TARGET_EXCEEDED":
                LocalDateTime startToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                if ("ZONE".equalsIgnoreCase(rule.getTargetType()) && rule.getTargetId() != null) {
                    Optional<Zone> zOpt = zoneRepository.findById(rule.getTargetId());
                    if (zOpt.isPresent()) {
                        targetZone = zOpt.get();
                        targetBuilding = targetZone.getBuilding();
                        Double consumed = readingRepository.sumEnergyByZoneBetween(targetZone.getId(), startToday, LocalDateTime.now());
                        if (consumed > rule.getThresholdValue()) {
                            conditionTriggered = true;
                            reason = String.format("Zone %s energy today (%.2f kWh) exceeded target (%.2f kWh)",
                                    targetZone.getName(), consumed, rule.getThresholdValue());
                        }
                    }
                } else {
                    Double totalToday = readingRepository.sumEnergyBetween(startToday, LocalDateTime.now());
                    if (totalToday > rule.getThresholdValue()) {
                        conditionTriggered = true;
                        reason = String.format("Total building energy today (%.2f kWh) exceeded limit (%.2f kWh)",
                                totalToday, rule.getThresholdValue());
                    }
                }
                break;

            case "DEVICE_OFFLINE":
                long offlineCount = deviceRepository.countByIsOnlineFalse();
                if (offlineCount >= rule.getThresholdValue()) {
                    conditionTriggered = true;
                    reason = String.format("Detected %d offline IoT devices exceeding threshold (%d)",
                            offlineCount, rule.getThresholdValue().longValue());
                }
                break;

            case "ABNORMAL_CONSUMPTION":
                Double recentAvg = readingRepository.avgPowerSince(LocalDateTime.now().minusMinutes(10));
                if (recentAvg > rule.getThresholdValue()) {
                    conditionTriggered = true;
                    reason = String.format("Abnormal 10-minute average power spike detected (%.1f W > %.1f W)",
                            recentAvg, rule.getThresholdValue());
                }
                break;

            default:
                break;
        }

        if (conditionTriggered) {
            rule.setLastTriggeredAt(LocalDateTime.now());
            ruleRepository.save(rule);

            // Execute safe simulated action and record execution audit
            String actionTaken = switch (rule.getActionType()) {
                case "SIMULATED_LOAD_SHEDDING" -> "Executed simulated dynamic load shedding. Non-essential circuits flagged for power throttle.";
                case "AUTO_DIM_LIGHTING_SIMULATED" -> "Dispatched simulated BACnet command to dim ambient perimeter lighting by 40%.";
                case "SET_ECO_MODE_SIMULATED" -> "Applied simulated HVAC Eco-Setback thermostat profile (increased setpoint by +1.5 C).";
                default -> "Generated administrative dispatch alert and logged automated advisory notice.";
            };

            AutomationExecution execution = new AutomationExecution(
                    rule,
                    reason,
                    actionTaken,
                    "SIMULATED_SUCCESS",
                    "Safety Note: Automated action executed in demonstration/simulated mode without physical breaker tripping."
            );
            executionRepository.save(execution);

            // Create alert
            alertService.triggerAlertWithCooldown(
                    "Automation Rule Triggered: " + rule.getName(),
                    reason + ". " + actionTaken,
                    "HIGH",
                    "RULE_TRIGGERED",
                    targetDevice,
                    targetZone,
                    targetBuilding,
                    rule.getCooldownMinutes()
            );

            logger.info("Automation rule triggered [{}]: {}", rule.getName(), reason);
        } else {
            ruleRepository.save(rule);
        }
    }

    public AutomationRuleDto mapToDto(AutomationRule rule) {
        AutomationRuleDto dto = new AutomationRuleDto();
        dto.setId(rule.getId());
        dto.setName(rule.getName());
        dto.setDescription(rule.getDescription());
        dto.setTargetType(rule.getTargetType());
        dto.setTargetId(rule.getTargetId());
        dto.setTargetName(rule.getTargetName());
        dto.setConditionType(rule.getConditionType());
        dto.setThresholdValue(rule.getThresholdValue());
        dto.setEvaluationIntervalSeconds(rule.getEvaluationIntervalSeconds());
        dto.setCooldownMinutes(rule.getCooldownMinutes());
        dto.setActionType(rule.getActionType());
        dto.setEnabled(rule.isEnabled());
        dto.setLastEvaluatedAt(rule.getLastEvaluatedAt());
        dto.setLastTriggeredAt(rule.getLastTriggeredAt());
        dto.setCreatedAt(rule.getCreatedAt());
        return dto;
    }

    public AutomationExecutionDto mapExecutionToDto(AutomationExecution exec) {
        AutomationExecutionDto dto = new AutomationExecutionDto();
        dto.setId(exec.getId());
        dto.setRuleId(exec.getRule() != null ? exec.getRule().getId() : null);
        dto.setRuleName(exec.getRule() != null ? exec.getRule().getName() : "Rule");
        dto.setTriggerReason(exec.getTriggerReason());
        dto.setActionTaken(exec.getActionTaken());
        dto.setStatus(exec.getStatus());
        dto.setDetails(exec.getDetails());
        dto.setExecutedAt(exec.getExecutedAt());
        return dto;
    }
}

