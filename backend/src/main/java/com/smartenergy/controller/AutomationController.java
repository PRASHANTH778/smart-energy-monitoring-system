package com.smartenergy.controller;

import com.smartenergy.dto.AutomationExecutionDto;
import com.smartenergy.dto.AutomationRuleDto;
import com.smartenergy.service.AutomationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {

    private final AutomationService automationService;

    public AutomationController(AutomationService automationService) {
        this.automationService = automationService;
    }

    @GetMapping("/rules")
    public ResponseEntity<List<AutomationRuleDto>> getAllRules() {
        return ResponseEntity.ok(automationService.getAllRules());
    }

    @GetMapping("/rules/{id}")
    public ResponseEntity<AutomationRuleDto> getRuleById(@PathVariable Long id) {
        return ResponseEntity.ok(automationService.getRuleById(id));
    }

    @PostMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AutomationRuleDto> createRule(@Valid @RequestBody AutomationRuleDto dto) {
        return new ResponseEntity<>(automationService.createRule(dto), HttpStatus.CREATED);
    }

    @PutMapping("/rules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<AutomationRuleDto> updateRule(@PathVariable Long id, @Valid @RequestBody AutomationRuleDto dto) {
        return ResponseEntity.ok(automationService.updateRule(id, dto));
    }

    @DeleteMapping("/rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteRule(@PathVariable Long id) {
        automationService.deleteRule(id);
        return ResponseEntity.ok(Map.of("message", "Automation rule deleted successfully"));
    }

    @GetMapping("/executions")
    public ResponseEntity<List<AutomationExecutionDto>> getRecentExecutions() {
        return ResponseEntity.ok(automationService.getRecentExecutions());
    }
}

