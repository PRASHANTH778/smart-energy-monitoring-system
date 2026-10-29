package com.smartenergy.controller;

import com.smartenergy.dto.IoTDeviceDto;
import com.smartenergy.service.IoTDeviceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class IoTDeviceController {

    private final IoTDeviceService deviceService;

    public IoTDeviceController(IoTDeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<IoTDeviceDto>> getAllDevices(@RequestParam(required = false) Long zoneId) {
        return ResponseEntity.ok(deviceService.getAllDevices(zoneId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IoTDeviceDto> getDeviceById(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<IoTDeviceDto> createDevice(@Valid @RequestBody IoTDeviceDto dto) {
        return new ResponseEntity<>(deviceService.createDevice(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<IoTDeviceDto> updateDevice(@PathVariable Long id, @Valid @RequestBody IoTDeviceDto dto) {
        return ResponseEntity.ok(deviceService.updateDevice(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(Map.of("message", "Device deleted successfully"));
    }

    @PostMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'OPERATOR')")
    public ResponseEntity<IoTDeviceDto> toggleDeviceState(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.toggleDeviceState(id));
    }
}

