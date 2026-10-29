package com.smartenergy.service;

import com.smartenergy.dto.BuildingDto;
import com.smartenergy.exception.BadRequestException;
import com.smartenergy.exception.ResourceNotFoundException;
import com.smartenergy.entity.Building;
import com.smartenergy.repository.BuildingRepository;
import com.smartenergy.repository.ZoneRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final ZoneRepository zoneRepository;

    public BuildingService(BuildingRepository buildingRepository, ZoneRepository zoneRepository) {
        this.buildingRepository = buildingRepository;
        this.zoneRepository = zoneRepository;
    }

    public List<BuildingDto> getAllBuildings() {
        return buildingRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public BuildingDto getBuildingById(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));
        return mapToDto(building);
    }

    @Transactional
    public BuildingDto createBuilding(BuildingDto dto) {
        if (buildingRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Building with name '" + dto.getName() + "' already exists");
        }

        Building building = new Building(
                dto.getName(),
                dto.getDescription(),
                dto.getLocation(),
                dto.getBuildingType()
        );
        building.setActive(dto.isActive());

        return mapToDto(buildingRepository.save(building));
    }

    @Transactional
    public BuildingDto updateBuilding(Long id, BuildingDto dto) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));

        building.setName(dto.getName());
        building.setDescription(dto.getDescription());
        building.setLocation(dto.getLocation());
        building.setBuildingType(dto.getBuildingType());
        building.setActive(dto.isActive());

        return mapToDto(buildingRepository.save(building));
    }

    @Transactional
    public void deleteBuilding(Long id) {
        Building building = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with id: " + id));

        int zoneCount = zoneRepository.findByBuildingId(id).size();
        if (zoneCount > 0) {
            throw new BadRequestException("Cannot delete building with active zones. Please remove or reassign associated zones first.");
        }

        buildingRepository.delete(building);
    }

    public BuildingDto mapToDto(Building building) {
        BuildingDto dto = new BuildingDto();
        dto.setId(building.getId());
        dto.setName(building.getName());
        dto.setDescription(building.getDescription());
        dto.setLocation(building.getLocation());
        dto.setBuildingType(building.getBuildingType());
        dto.setActive(building.isActive());
        dto.setCreatedAt(building.getCreatedAt());

        if (building.getZones() != null) {
            dto.setZoneCount(building.getZones().size());
            int devCount = building.getZones().stream()
                    .mapToInt(z -> z.getDevices() != null ? z.getDevices().size() : 0)
                    .sum();
            dto.setDeviceCount(devCount);
        }
        return dto;
    }
}

