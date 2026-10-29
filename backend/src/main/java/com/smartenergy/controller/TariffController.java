package com.smartenergy.controller;

import com.smartenergy.dto.EnergyTariffDto;
import com.smartenergy.service.TariffService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tariffs")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    @GetMapping
    public ResponseEntity<List<EnergyTariffDto>> getAllTariffs() {
        return ResponseEntity.ok(tariffService.getAllTariffs());
    }

    @GetMapping("/active")
    public ResponseEntity<EnergyTariffDto> getActiveTariff() {
        return ResponseEntity.ok(tariffService.getActiveTariff());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnergyTariffDto> createTariff(@Valid @RequestBody EnergyTariffDto dto) {
        return new ResponseEntity<>(tariffService.createTariff(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnergyTariffDto> updateTariff(@PathVariable Long id, @Valid @RequestBody EnergyTariffDto dto) {
        return ResponseEntity.ok(tariffService.updateTariff(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteTariff(@PathVariable Long id) {
        tariffService.deleteTariff(id);
        return ResponseEntity.ok(Map.of("message", "Tariff deleted successfully"));
    }
}

