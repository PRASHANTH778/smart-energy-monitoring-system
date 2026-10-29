package com.smartenergy.controller;

import com.smartenergy.dto.DashboardSummaryDto;
import com.smartenergy.dto.EnergyAnalyticsDto;
import com.smartenergy.dto.SensorReadingDto;
import com.smartenergy.service.EnergyAnalyticsService;
import com.smartenergy.service.SensorReadingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Energy Readings & Analytics REST Controller
 */
@RestController
@RequestMapping("/api/energy")
public class EnergyController {

    private final EnergyAnalyticsService analyticsService;
    private final SensorReadingService readingService;

    public EnergyController(EnergyAnalyticsService analyticsService, SensorReadingService readingService) {
        this.analyticsService = analyticsService;
        this.readingService = readingService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> getSummary() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/latest")
    public ResponseEntity<List<SensorReadingDto>> getLatestReadings() {
        return ResponseEntity.ok(readingService.getRecentReadings());
    }

    @GetMapping("/analytics")
    public ResponseEntity<EnergyAnalyticsDto> getAnalytics(@RequestParam(defaultValue = "today") String range) {
        return ResponseEntity.ok(analyticsService.getAnalytics(range));
    }

    @GetMapping("/readings")
    public ResponseEntity<Page<SensorReadingDto>> getReadings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(readingService.getReadings(pageable));
    }
}

