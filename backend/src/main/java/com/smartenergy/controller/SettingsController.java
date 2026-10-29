package com.smartenergy.controller;

import com.smartenergy.iot.IoTSimulationService;
import com.smartenergy.iot.MqttGatewayService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final IoTSimulationService simulationService;
    private final MqttGatewayService mqttGatewayService;

    @Value("${app.mqtt.broker-url:tcp://localhost:1883}")
    private String mqttBrokerUrl;

    @Value("${app.mqtt.topic-filter:smartbuilding/#}")
    private String mqttTopic;

    @Value("${app.device.offline-threshold-seconds:60}")
    private long offlineThreshold;

    public SettingsController(IoTSimulationService simulationService, MqttGatewayService mqttGatewayService) {
        this.simulationService = simulationService;
        this.mqttGatewayService = mqttGatewayService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSettings() {
        Map<String, Object> settings = new HashMap<>();
        settings.put("simulationEnabled", simulationService.isSimulationEnabled());
        settings.put("mqttConnected", mqttGatewayService.isConnected());
        settings.put("mqttBrokerUrl", mqttBrokerUrl);
        settings.put("mqttTopicPattern", mqttTopic);
        settings.put("offlineThresholdSeconds", offlineThreshold);
        settings.put("systemVersion", "2.4.0-RELEASE");
        settings.put("springBootVersion", "3.3.4");
        settings.put("hardwareArchitecture", "ESP32 SoC + PZEM-004T / CT Clamps");
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/simulation/toggle")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Map<String, Object>> toggleSimulation(@RequestBody Map<String, Boolean> payload) {
        Boolean enabled = payload.get("enabled");
        if (enabled != null) {
            simulationService.setSimulationEnabled(enabled);
        }
        return ResponseEntity.ok(Map.of(
                "simulationEnabled", simulationService.isSimulationEnabled(),
                "message", "Simulation mode updated to " + simulationService.isSimulationEnabled()
        ));
    }
}
