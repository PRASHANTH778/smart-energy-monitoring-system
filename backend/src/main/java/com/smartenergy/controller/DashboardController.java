package com.smartenergy.controller;

import com.smartenergy.dto.DashboardSummaryDto;
import com.smartenergy.service.EnergyAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final EnergyAnalyticsService analyticsService;

    public DashboardController(EnergyAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getDashboardSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }
}

