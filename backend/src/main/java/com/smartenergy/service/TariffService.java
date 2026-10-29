package com.smartenergy.service;

import com.smartenergy.dto.EnergyTariffDto;
import com.smartenergy.exception.ResourceNotFoundException;
import com.smartenergy.entity.EnergyTariff;
import com.smartenergy.repository.EnergyTariffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TariffService {

    private final EnergyTariffRepository tariffRepository;

    public TariffService(EnergyTariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    public List<EnergyTariffDto> getAllTariffs() {
        return tariffRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public EnergyTariffDto getActiveTariff() {
        EnergyTariff tariff = tariffRepository.findFirstByActiveTrueOrderByEffectiveFromDesc()
                .orElseThrow(() -> new ResourceNotFoundException("No active energy tariff configured"));
        return mapToDto(tariff);
    }

    @Transactional
    public EnergyTariffDto createTariff(EnergyTariffDto dto) {
        EnergyTariff tariff = new EnergyTariff(
                dto.getName(),
                dto.getRatePerKwh(),
                dto.getEffectiveFrom(),
                dto.getEffectiveTo(),
                dto.getBaselineTargetKwh(),
                dto.getCurrency()
        );
        tariff.setActive(dto.isActive());
        return mapToDto(tariffRepository.save(tariff));
    }

    @Transactional
    public EnergyTariffDto updateTariff(Long id, EnergyTariffDto dto) {
        EnergyTariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found with id: " + id));

        tariff.setName(dto.getName());
        tariff.setRatePerKwh(dto.getRatePerKwh());
        tariff.setEffectiveFrom(dto.getEffectiveFrom());
        tariff.setEffectiveTo(dto.getEffectiveTo());
        tariff.setBaselineTargetKwh(dto.getBaselineTargetKwh());
        tariff.setCurrency(dto.getCurrency());
        tariff.setActive(dto.isActive());

        return mapToDto(tariffRepository.save(tariff));
    }

    @Transactional
    public void deleteTariff(Long id) {
        EnergyTariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found with id: " + id));
        tariffRepository.delete(tariff);
    }

    public EnergyTariffDto mapToDto(EnergyTariff tariff) {
        EnergyTariffDto dto = new EnergyTariffDto();
        dto.setId(tariff.getId());
        dto.setName(tariff.getName());
        dto.setRatePerKwh(tariff.getRatePerKwh());
        dto.setEffectiveFrom(tariff.getEffectiveFrom());
        dto.setEffectiveTo(tariff.getEffectiveTo());
        dto.setBaselineTargetKwh(tariff.getBaselineTargetKwh());
        dto.setCurrency(tariff.getCurrency());
        dto.setActive(tariff.isActive());
        return dto;
    }
}

