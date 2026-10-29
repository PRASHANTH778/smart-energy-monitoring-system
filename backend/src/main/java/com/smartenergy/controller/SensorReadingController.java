package com.smartenergy.controller;

import com.smartenergy.dto.SensorReadingDto;
import com.smartenergy.dto.TelemetryPayload;
import com.smartenergy.service.SensorReadingService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/readings")
public class SensorReadingController {

    private final SensorReadingService readingService;

    public SensorReadingController(SensorReadingService readingService) {
        this.readingService = readingService;
    }

    /**
     * Ingestion endpoint for ESP32 and edge sensor nodes
     */
    @PostMapping("/ingest")
    public ResponseEntity<SensorReadingDto> ingestTelemetry(@Valid @RequestBody TelemetryPayload payload) {
        return new ResponseEntity<>(readingService.processTelemetry(payload, false), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<SensorReadingDto>> getReadings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(readingService.getReadings(pageable));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<SensorReadingDto>> getRecentReadings() {
        return ResponseEntity.ok(readingService.getRecentReadings());
    }
}

