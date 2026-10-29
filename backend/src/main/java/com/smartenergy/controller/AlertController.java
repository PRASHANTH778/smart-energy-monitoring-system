package com.smartenergy.controller;

import com.smartenergy.dto.AlertActionRequest;
import com.smartenergy.dto.AlertDto;
import com.smartenergy.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<Page<AlertDto>> getAlerts(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) Boolean isResolved,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(alertService.getAlerts(severity, isResolved, pageable));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AlertDto>> getRecentAlerts() {
        return ResponseEntity.ok(alertService.getRecentAlerts());
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getAlertCount() {
        return ResponseEntity.ok(Map.of("activeAlerts", alertService.getActiveAlertCount()));
    }

    @PostMapping("/{id}/action")
    public ResponseEntity<AlertDto> processAlertAction(
            @PathVariable Long id,
            @Valid @RequestBody AlertActionRequest request) {
        return ResponseEntity.ok(alertService.processAlertAction(id, request));
    }

    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<AlertDto> acknowledgeAlert(@PathVariable Long id) {
        AlertActionRequest req = new AlertActionRequest();
        req.setAction("ACKNOWLEDGE");
        return ResponseEntity.ok(alertService.processAlertAction(id, req));
    }

    @PutMapping("/{id}/resolve")
    public ResponseEntity<AlertDto> resolveAlert(@PathVariable Long id) {
        AlertActionRequest req = new AlertActionRequest();
        req.setAction("RESOLVE");
        return ResponseEntity.ok(alertService.processAlertAction(id, req));
    }
}
