package com.smartenergy.service;

import com.smartenergy.dto.ZoneDto;
import com.smartenergy.exception.BadRequestException;
import com.smartenergy.exception.ResourceNotFoundException;
import com.smartenergy.entity.Building;
import com.smartenergy.entity.Zone;
import com.smartenergy.repository.BuildingRepository;
import com.smartenergy.repository.IoTDeviceRepository;
import com.smartenergy.repository.SensorReadingRepository;
import com.smartenergy.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ZoneService {

    private final ZoneRepository zoneRepository;
    private final BuildingRepository buildingRepository;
    private final IoTDeviceRepository deviceRepository;
    private final SensorReadingRepository readingRepository;

    public ZoneService(ZoneRepository zoneRepository, BuildingRepository buildingRepository,
                       IoTDeviceRepository deviceRepository, SensorReadingRepository readingRepository) {
        this.zoneRepository = zoneRepository;
        this.buildingRepository = buildingRepository;
        this.deviceRepository = deviceRepository;
        this.readingRepository = readingRepository;
    }

    public List<ZoneDto> getAllZones(Long buildingId) {
        List<Zone> zones;
        if (buildingId != null) {
            zones = zoneRepository.findByBuildingId(buildingId);
        } else {
            zones = zoneRepository.findAll();
        }

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();

        return zones.stream()
                .map(z -> mapToDto(z, startOfToday, now))
                .collect(Collectors.toList());
    }

    public ZoneDto getZoneById(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        LocalDateTime startOfToday = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime now = LocalDateTime.now();
        return mapToDto(zone, startOfToday, now);
    }

    @Transactional
    public ZoneDto createZone(ZoneDto dto) {
        Building building = buildingRepository.findById(dto.getBuildingId())
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + dto.getBuildingId()));

        Zone zone = new Zone(
                dto.getName(),
                dto.getDescription(),
                building,
                dto.getDailyEnergyTargetKwh()
        );
        zone.setActive(dto.isActive());

        Zone saved = zoneRepository.save(zone);
        return mapToDto(saved, LocalDateTime.now(), LocalDateTime.now());
    }

    @Transactional
    public ZoneDto updateZone(Long id, ZoneDto dto) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        if (dto.getBuildingId() != null && !dto.getBuildingId().equals(zone.getBuilding().getId())) {
            Building building = buildingRepository.findById(dto.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + dto.getBuildingId()));
            zone.setBuilding(building);
        }

        zone.setName(dto.getName());
        zone.setDescription(dto.getDescription());
        if (dto.getDailyEnergyTargetKwh() != null) {
            zone.setDailyEnergyTargetKwh(dto.getDailyEnergyTargetKwh());
        }
        zone.setActive(dto.isActive());

        Zone saved = zoneRepository.save(zone);
        return mapToDto(saved, LocalDateTime.now(), LocalDateTime.now());
    }

    @Transactional
    public void deleteZone(Long id) {
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone not found with id: " + id));

        int deviceCount = deviceRepository.findByZoneId(id).size();
        if (deviceCount > 0) {
            throw new BadRequestException("Cannot delete zone with assigned IoT devices. Please reassign or remove devices first.");
        }

        zoneRepository.delete(zone);
    }

    private ZoneDto mapToDto(Zone zone, LocalDateTime startToday, LocalDateTime now) {
        ZoneDto dto = new ZoneDto();
        dto.setId(zone.getId());
        dto.setName(zone.getName());
        dto.setDescription(zone.getDescription());
        dto.setDailyEnergyTargetKwh(zone.getDailyEnergyTargetKwh());
        dto.setActive(zone.isActive());
        dto.setCreatedAt(zone.getCreatedAt());

        if (zone.getBuilding() != null) {
            dto.setBuildingId(zone.getBuilding().getId());
            dto.setBuildingName(zone.getBuilding().getName());
        }

        int devCount = deviceRepository.findByZoneId(zone.getId()).size();
        dto.setDeviceCount(devCount);

        Double todayEnergy = readingRepository.sumEnergyByZoneBetween(zone.getId(), startToday, now);
        dto.setCurrentConsumptionTodayKwh(Math.round(todayEnergy * 100.0) / 100.0);

        return dto;
    }
}

