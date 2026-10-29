package com.smartenergy.iot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.smartenergy.dto.TelemetryPayload;
import com.smartenergy.service.SensorReadingService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * MQTT Ingestion Gateway for Edge Hardware Microcontrollers (e.g. ESP32, Raspberry Pi).
 */
@Component
public class MqttGatewayService implements MqttCallbackExtended {

    private static final Logger logger = LoggerFactory.getLogger(MqttGatewayService.class);

    private final SensorReadingService readingService;
    private final ObjectMapper objectMapper;
    private MqttAsyncClient mqttClient;

    @Value("${app.mqtt.enabled:false}")
    private boolean mqttEnabled;

    @Value("${app.mqtt.broker-url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${app.mqtt.client-id:smart-energy-backend}")
    private String clientId;

    @Value("${app.mqtt.topic-filter:smartbuilding/#}")
    private String topicFilter;

    @Value("${app.mqtt.username:}")
    private String mqttUsername;

    @Value("${app.mqtt.password:}")
    private String mqttPassword;

    public MqttGatewayService(SensorReadingService readingService) {
        this.readingService = readingService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostConstruct
    public void init() {
        if (!mqttEnabled) {
            logger.info("[MQTT] External MQTT gateway disabled. Running with internal IoT Simulation Engine.");
            return;
        }

        connectToBroker();
    }

    public synchronized void connectToBroker() {
        try {
            logger.info("[MQTT] Attempting connection to MQTT Broker: {}", brokerUrl);
            mqttClient = new MqttAsyncClient(brokerUrl, clientId + "-" + System.currentTimeMillis(), new MemoryPersistence());
            mqttClient.setCallback(this);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(5);
            options.setKeepAliveInterval(30);

            if (mqttUsername != null && !mqttUsername.isBlank()) {
                options.setUserName(mqttUsername);
                options.setPassword(mqttPassword != null ? mqttPassword.toCharArray() : new char[0]);
            }

            mqttClient.connect(options, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    logger.info("[MQTT] Connected to MQTT broker: {}", brokerUrl);
                    subscribeToTopics();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    logger.warn("[MQTT] Connection to broker ({}) skipped: {}. Falling back to simulation.",
                            brokerUrl, exception.getMessage());
                }
            });

        } catch (MqttException e) {
            logger.warn("[MQTT] Broker connection initialization error: {}. Defaulting to simulation.", e.getMessage());
        }
    }

    private void subscribeToTopics() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.subscribe(topicFilter, 1, (topic, message) -> {
                    processIncomingMqttMessage(topic, message);
                });
                logger.info("[MQTT] Subscribed to topic pattern: {}", topicFilter);
            }
        } catch (MqttException e) {
            logger.error("[MQTT] Error subscribing to topic {}: {}", topicFilter, e.getMessage());
        }
    }

    private void processIncomingMqttMessage(String topic, MqttMessage message) {
        String payloadString = new String(message.getPayload());
        logger.debug("[MQTT] Inbound message on topic '{}': {}", topic, payloadString);

        try {
            TelemetryPayload payload = objectMapper.readValue(payloadString, TelemetryPayload.class);

            if (payload.getDeviceId() == null && topic.contains("/")) {
                String[] parts = topic.split("/");
                if (parts.length >= 4) {
                    payload.setDeviceId(parts[3]);
                }
            }

            readingService.processTelemetry(payload, false);
        } catch (Exception ex) {
            logger.error("[MQTT] Invalid telemetry payload on topic '{}': {}", topic, ex.getMessage());
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) {
            logger.info("[MQTT] Reconnected to broker: {}", serverURI);
            subscribeToTopics();
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        logger.warn("[MQTT] Lost broker connection: {}. Reconnect active.",
                cause != null ? cause.getMessage() : "Unknown");
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    @PreDestroy
    public void shutdown() {
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
                mqttClient.close();
                logger.info("[MQTT] Disconnected cleanly.");
            } catch (MqttException e) {
                logger.error("[MQTT] Disconnect error: {}", e.getMessage());
            }
        }
    }

    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }
}

