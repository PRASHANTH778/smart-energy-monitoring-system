package com.smartenergy.controller;

import com.smartenergy.dto.EnergyAnalyticsDto;
import com.smartenergy.service.EnergyAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class EnergyAnalyticsController {

    private final EnergyAnalyticsService analyticsService;

    public EnergyAnalyticsController(EnergyAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public ResponseEntity<EnergyAnalyticsDto> getAnalytics(@RequestParam(defaultValue = "today") String range) {
        return ResponseEntity.ok(analyticsService.getAnalytics(range));
    }
}

