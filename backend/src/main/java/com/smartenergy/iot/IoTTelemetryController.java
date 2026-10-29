package com.smartenergy.iot;

import com.smartenergy.dto.SensorReadingDto;
import com.smartenergy.dto.TelemetryPayload;
import com.smartenergy.service.SensorReadingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller exposing IoT integration endpoints for smart meters, field microcontrollers,
 * and presentation simulation controls.
 */
@RestController
@RequestMapping("/api/iot")
public class IoTTelemetryController {

    private final IoTSimulationService simulationService;
    private final SensorReadingService readingService;
    private final MqttGatewayService mqttGatewayService;

    public IoTTelemetryController(IoTSimulationService simulationService,
                                  SensorReadingService readingService,
                                  MqttGatewayService mqttGatewayService) {
        this.simulationService = simulationService;
        this.readingService = readingService;
        this.mqttGatewayService = mqttGatewayService;
    }

    @PostMapping("/telemetry")
    public ResponseEntity<SensorReadingDto> ingestTelemetry(@Valid @RequestBody TelemetryPayload payload) {
        return new ResponseEntity<>(readingService.processTelemetry(payload, false), HttpStatus.CREATED);
    }

    @PostMapping("/spike/{deviceId}")
    public ResponseEntity<Map<String, Object>> triggerSpike(@PathVariable String deviceId) {
        boolean success = simulationService.triggerSpikeOnDevice(deviceId);
        if (success) {
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "deviceId", deviceId,
                    "message", "Simulated electrical power surge successfully injected into " + deviceId
            ));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", "ERROR",
                    "message", "Device identifier not found: " + deviceId
            ));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
                "simulationEnabled", simulationService.isSimulationEnabled(),
                "mqttConnected", mqttGatewayService.isConnected(),
                "engineStatus", "ONLINE"
        ));
    }

    @PostMapping("/toggle")
    public ResponseEntity<Map<String, Object>> toggleSimulation(@RequestBody Map<String, Boolean> payload) {
        Boolean enabled = payload.get("enabled");
        if (enabled != null) {
            simulationService.setSimulationEnabled(enabled);
        }
        return ResponseEntity.ok(Map.of(
                "simulationEnabled", simulationService.isSimulationEnabled(),
                "message", "IoT simulation state updated"
        ));
    }
}

