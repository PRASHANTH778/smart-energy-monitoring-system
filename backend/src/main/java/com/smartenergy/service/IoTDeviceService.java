package com.smartenergy.service;

import com.smartenergy.dto.IoTDeviceDto;
import com.smartenergy.exception.BadRequestException;
import com.smartenergy.exception.ResourceNotFoundException;
import com.smartenergy.entity.IoTDevice;
import com.smartenergy.entity.SensorReading;
import com.smartenergy.entity.Zone;
import com.smartenergy.repository.IoTDeviceRepository;
import com.smartenergy.repository.SensorReadingRepository;
import com.smartenergy.repository.ZoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class IoTDeviceService {

    private static final Logger logger = LoggerFactory.getLogger(IoTDeviceService.class);

    private final IoTDeviceRepository deviceRepository;
    private final ZoneRepository zoneRepository;
    private final SensorReadingRepository readingRepository;

    @Value("${app.device.offline-threshold-seconds:60}")
    private long offlineThresholdSeconds;

    public IoTDeviceService(IoTDeviceRepository deviceRepository, ZoneRepository zoneRepository,
                            SensorReadingRepository readingRepository) {
        this.deviceRepository = deviceRepository;
        this.zoneRepository = zoneRepository;
        this.readingRepository = readingRepository;
    }

    public List<IoTDeviceDto> getAllDevices(Long zoneId) {
        List<IoTDevice> devices;
        if (zoneId != null) {
            devices = deviceRepository.findByZoneId(zoneId);
        } else {
            devices = deviceRepository.findAll();
        }

        return devices.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public IoTDeviceDto getDeviceById(Long id) {
        IoTDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        return mapToDto(device);
    }

    public Optional<IoTDevice> findByDeviceId(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    @Transactional
    public IoTDeviceDto createDevice(IoTDeviceDto dto) {
        if (deviceRepository.findByDeviceId(dto.getDeviceId()).isPresent()) {
            throw new BadRequestException("Device with ID '" + dto.getDeviceId() + "' already exists");
        }

        Zone zone = zoneRepository.findById(dto.getZoneId())
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + dto.getZoneId()));

        IoTDevice device = new IoTDevice(
                dto.getDeviceId(),
                dto.getName(),
                dto.getDeviceType() != null ? dto.getDeviceType() : "ESP32_PZEM004T",
                zone,
                dto.getProtocol() != null ? dto.getProtocol() : "MQTT",
                dto.getFirmwareVersion() != null ? dto.getFirmwareVersion() : "1.0.0-PROD",
                dto.isSimulationEnabled()
        );
        device.setActive(dto.isActive());
        device.setOnline(false);
        device.setLastSeenAt(null);

        return mapToDto(deviceRepository.save(device));
    }

    @Transactional
    public IoTDeviceDto updateDevice(Long id, IoTDeviceDto dto) {
        IoTDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));

        if (!device.getDeviceId().equalsIgnoreCase(dto.getDeviceId())) {
            if (deviceRepository.findByDeviceId(dto.getDeviceId()).isPresent()) {
                throw new BadRequestException("Device ID '" + dto.getDeviceId() + "' already in use");
            }
            device.setDeviceId(dto.getDeviceId());
        }

        if (dto.getZoneId() != null && !dto.getZoneId().equals(device.getZone().getId())) {
            Zone zone = zoneRepository.findById(dto.getZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + dto.getZoneId()));
            device.setZone(zone);
        }

        device.setName(dto.getName());
        device.setDeviceType(dto.getDeviceType());
        device.setProtocol(dto.getProtocol());
        if (dto.getFirmwareVersion() != null) {
            device.setFirmwareVersion(dto.getFirmwareVersion());
        }
        device.setActive(dto.isActive());
        device.setSimulationEnabled(dto.isSimulationEnabled());

        return mapToDto(deviceRepository.save(device));
    }

    @Transactional
    public void deleteDevice(Long id) {
        IoTDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        deviceRepository.delete(device);
    }

    @Transactional
    public IoTDeviceDto toggleDeviceState(Long id) {
        IoTDevice device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
        device.setActive(!device.isActive());
        if (!device.isActive()) {
            device.setOnline(false);
        }
        return mapToDto(deviceRepository.save(device));
    }

    /**
     * Heartbeat / Telemetry touch: marks device online and updates lastSeenAt timestamp
     */
    @Transactional
    public void recordHeartbeat(IoTDevice device) {
        device.setLastSeenAt(LocalDateTime.now());
        device.setOnline(true);
        deviceRepository.save(device);
    }

    /**
     * Scheduled heartbeat check every 15 seconds:
     * Evaluates if any active device has not sent a reading within the threshold.
     */
    @Scheduled(fixedRate = 15000)
    @Transactional
    public void checkDeviceConnectivity() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(offlineThresholdSeconds);
        List<IoTDevice> devices = deviceRepository.findAll();

        for (IoTDevice device : devices) {
            boolean shouldBeOnline = device.getLastSeenAt() != null && device.getLastSeenAt().isAfter(threshold);
            if (device.isOnline() != shouldBeOnline) {
                device.setOnline(shouldBeOnline);
                deviceRepository.save(device);
                if (!shouldBeOnline) {
                    logger.warn("Device {} ({}) is now OFFLINE (last seen: {})",
                            device.getDeviceId(), device.getName(), device.getLastSeenAt());
                }
            }
        }
    }

    public IoTDeviceDto mapToDto(IoTDevice device) {
        IoTDeviceDto dto = new IoTDeviceDto();
        dto.setId(device.getId());
        dto.setDeviceId(device.getDeviceId());
        dto.setName(device.getName());
        dto.setDeviceType(device.getDeviceType());
        dto.setProtocol(device.getProtocol());
        dto.setFirmwareVersion(device.getFirmwareVersion());
        dto.setLastSeenAt(device.getLastSeenAt());

        // Dynamic online check based on threshold
        boolean isRecentlySeen = device.getLastSeenAt() != null &&
                device.getLastSeenAt().isAfter(LocalDateTime.now().minusSeconds(offlineThresholdSeconds));
        dto.setOnline(isRecentlySeen);

        dto.setActive(device.isActive());
        dto.setSimulationEnabled(device.isSimulationEnabled());
        dto.setCreatedAt(device.getCreatedAt());

        if (device.getZone() != null) {
            dto.setZoneId(device.getZone().getId());
            dto.setZoneName(device.getZone().getName());
            if (device.getZone().getBuilding() != null) {
                dto.setBuildingId(device.getZone().getBuilding().getId());
                dto.setBuildingName(device.getZone().getBuilding().getName());
            }
        }

        // Fetch latest reading cache
        Optional<SensorReading> latest = readingRepository.findTopByDeviceIdOrderByTimestampDesc(device.getId());
        if (latest.isPresent()) {
            SensorReading r = latest.get();
            dto.setCurrentVoltage(r.getVoltage());
            dto.setCurrentAmperes(r.getCurrent());
            dto.setCurrentPowerWatts(r.getPower());
            dto.setTotalEnergyKwh(r.getEnergyKwh());
        }

        return dto;
    }
}

