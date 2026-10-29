package com.smartenergy.service;

import com.smartenergy.dto.AlertActionRequest;
import com.smartenergy.dto.AlertDto;
import com.smartenergy.exception.BadRequestException;
import com.smartenergy.exception.ResourceNotFoundException;
import com.smartenergy.entity.Alert;
import com.smartenergy.entity.Building;
import com.smartenergy.entity.IoTDevice;
import com.smartenergy.entity.Zone;
import com.smartenergy.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public Page<AlertDto> getAlerts(String severity, Boolean isResolved, Pageable pageable) {
        return alertRepository.findFilteredAlerts(severity, isResolved, pageable)
                .map(this::mapToDto);
    }

    public List<AlertDto> getRecentAlerts() {
        return alertRepository.findTop10ByOrderByCreatedAtDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public long getActiveAlertCount() {
        return alertRepository.countByIsResolvedFalse();
    }

    @Transactional
    public Alert triggerAlertWithCooldown(String title, String description, String severity,
                                          String alertType, IoTDevice device, Zone zone, Building building,
                                          int cooldownMinutes) {
        Long deviceId = device != null ? device.getId() : null;
        Long zoneId = zone != null ? zone.getId() : null;

        LocalDateTime cooldownSince = LocalDateTime.now().minusMinutes(cooldownMinutes);
        List<Alert> existing = alertRepository.findRecentDuplicateAlerts(alertType, deviceId, zoneId, cooldownSince);

        if (!existing.isEmpty()) {
            logger.debug("Suppressed duplicate alert '{}' for device/zone within cooldown window ({} min)",
                    title, cooldownMinutes);
            return existing.get(0);
        }

        Alert alert = new Alert(title, description, severity, alertType, device, zone, building);
        Alert saved = alertRepository.save(alert);
        logger.info("New Alert Created: [{}] {} - {}", severity, title, description);
        return saved;
    }

    @Transactional
    public AlertDto processAlertAction(Long id, AlertActionRequest request) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + id));

        String currentUser = "System Admin";
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        }

        if ("ACKNOWLEDGE".equalsIgnoreCase(request.getAction())) {
            alert.setAcknowledged(true);
            alert.setAcknowledgedBy(currentUser);
            alert.setAcknowledgedAt(LocalDateTime.now());
        } else if ("RESOLVE".equalsIgnoreCase(request.getAction())) {
            alert.setResolved(true);
            alert.setResolvedBy(currentUser);
            alert.setResolvedAt(LocalDateTime.now());
            if (!alert.isAcknowledged()) {
                alert.setAcknowledged(true);
                alert.setAcknowledgedBy(currentUser);
                alert.setAcknowledgedAt(LocalDateTime.now());
            }
        } else {
            throw new BadRequestException("Invalid action: " + request.getAction() + ". Supported: ACKNOWLEDGE, RESOLVE");
        }

        return mapToDto(alertRepository.save(alert));
    }

    public AlertDto mapToDto(Alert alert) {
        AlertDto dto = new AlertDto();
        dto.setId(alert.getId());
        dto.setTitle(alert.getTitle());
        dto.setDescription(alert.getDescription());
        dto.setSeverity(alert.getSeverity());
        dto.setAlertType(alert.getAlertType());

        if (alert.getDevice() != null) {
            dto.setDeviceId(alert.getDevice().getId());
            dto.setDeviceIdentifier(alert.getDevice().getDeviceId());
            dto.setDeviceName(alert.getDevice().getName());
        }

        if (alert.getZone() != null) {
            dto.setZoneId(alert.getZone().getId());
            dto.setZoneName(alert.getZone().getName());
        }

        if (alert.getBuilding() != null) {
            dto.setBuildingId(alert.getBuilding().getId());
            dto.setBuildingName(alert.getBuilding().getName());
        }

        dto.setAcknowledged(alert.isAcknowledged());
        dto.setAcknowledgedBy(alert.getAcknowledgedBy());
        dto.setAcknowledgedAt(alert.getAcknowledgedAt());

        dto.setResolved(alert.isResolved());
        dto.setResolvedBy(alert.getResolvedBy());
        dto.setResolvedAt(alert.getResolvedAt());

        dto.setCreatedAt(alert.getCreatedAt());

        return dto;
    }
}

