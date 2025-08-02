package com.erp.system.edge.service;

import com.erp.system.edge.entity.IoTDevice;
import com.erp.system.edge.entity.SensorReading;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

/**
 * MQTT Gateway Service
 * Manages MQTT communication with IoT devices
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MqttGatewayService {

    private final EdgeComputingService edgeComputingService;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.client.id:erp-edge-gateway}")
    private String clientId;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    @Value("${mqtt.qos:1}")
    private int qos;

    @Value("${mqtt.keep.alive:60}")
    private int keepAlive;

    @Value("${mqtt.connection.timeout:30}")
    private int connectionTimeout;

    private MqttClient mqttClient;
    private final Map<String, IoTDevice> subscribedDevices = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        try {
            log.info("Initializing MQTT Gateway Service");
            connectToBroker();
            setupCallbacks();
            log.info("MQTT Gateway Service initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize MQTT Gateway Service", e);
            throw new RuntimeException("MQTT initialization failed", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                log.info("MQTT Gateway Service disconnected");
            }
        } catch (Exception e) {
            log.error("Error during MQTT cleanup", e);
        }
    }

    /**
     * Subscribe to device MQTT topic
     */
    public void subscribeToDevice(IoTDevice device) {
        try {
            if (device.getMqttTopic() == null || device.getMqttTopic().isEmpty()) {
                device.setMqttTopic("devices/" + device.getDeviceId() + "/data");
            }

            String topic = device.getMqttTopic();
            
            if (mqttClient.isConnected()) {
                mqttClient.subscribe(topic, qos);
                subscribedDevices.put(topic, device);
                log.info("Subscribed to device topic: {} for device: {}", 
                        topic, device.getDeviceId());
            } else {
                log.warn("MQTT client not connected, cannot subscribe to topic: {}", topic);
            }
        } catch (Exception e) {
            log.error("Failed to subscribe to device topic for device: {}", 
                     device.getDeviceId(), e);
        }
    }

    /**
     * Unsubscribe from device MQTT topic
     */
    public void unsubscribeFromDevice(IoTDevice device) {
        try {
            String topic = device.getMqttTopic();
            if (topic != null && mqttClient.isConnected()) {
                mqttClient.unsubscribe(topic);
                subscribedDevices.remove(topic);
                log.info("Unsubscribed from device topic: {} for device: {}", 
                        topic, device.getDeviceId());
            }
        } catch (Exception e) {
            log.error("Failed to unsubscribe from device topic for device: {}", 
                     device.getDeviceId(), e);
        }
    }

    /**
     * Publish command to device
     */
    @Async
    public CompletableFuture<Void> publishCommand(IoTDevice device, Object command) {
        return CompletableFuture.runAsync(() -> {
            try {
                String commandTopic = "devices/" + device.getDeviceId() + "/commands";
                String payload = objectMapper.writeValueAsString(command);
                
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(qos);
                message.setRetained(false);
                
                if (mqttClient.isConnected()) {
                    mqttClient.publish(commandTopic, message);
                    log.debug("Published command to device: {} on topic: {}", 
                             device.getDeviceId(), commandTopic);
                } else {
                    log.warn("MQTT client not connected, cannot publish command to device: {}", 
                            device.getDeviceId());
                }
            } catch (Exception e) {
                log.error("Failed to publish command to device: {}", 
                         device.getDeviceId(), e);
                throw new RuntimeException("Command publication failed", e);
            }
        });
    }

    /**
     * Publish configuration update to device
     */
    @Async
    public CompletableFuture<Void> publishConfiguration(IoTDevice device, Map<String, Object> config) {
        return CompletableFuture.runAsync(() -> {
            try {
                String configTopic = "devices/" + device.getDeviceId() + "/config";
                String payload = objectMapper.writeValueAsString(config);
                
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(qos);
                message.setRetained(true); // Retain config messages
                
                if (mqttClient.isConnected()) {
                    mqttClient.publish(configTopic, message);
                    log.info("Published configuration to device: {} on topic: {}", 
                            device.getDeviceId(), configTopic);
                } else {
                    log.warn("MQTT client not connected, cannot publish config to device: {}", 
                            device.getDeviceId());
                }
            } catch (Exception e) {
                log.error("Failed to publish configuration to device: {}", 
                         device.getDeviceId(), e);
                throw new RuntimeException("Configuration publication failed", e);
            }
        });
    }

    /**
     * Publish firmware update notification
     */
    @Async
    public CompletableFuture<Void> publishFirmwareUpdate(IoTDevice device, String firmwareUrl, String version) {
        return CompletableFuture.runAsync(() -> {
            try {
                String updateTopic = "devices/" + device.getDeviceId() + "/firmware";
                
                Map<String, Object> updateInfo = Map.of(
                    "url", firmwareUrl,
                    "version", version,
                    "timestamp", LocalDateTime.now().toString(),
                    "checksum", "sha256:..." // Would be calculated
                );
                
                String payload = objectMapper.writeValueAsString(updateInfo);
                
                MqttMessage message = new MqttMessage(payload.getBytes());
                message.setQos(2); // Exactly once for firmware updates
                message.setRetained(false);
                
                if (mqttClient.isConnected()) {
                    mqttClient.publish(updateTopic, message);
                    log.info("Published firmware update to device: {} version: {}", 
                            device.getDeviceId(), version);
                } else {
                    log.warn("MQTT client not connected, cannot publish firmware update to device: {}", 
                            device.getDeviceId());
                }
            } catch (Exception e) {
                log.error("Failed to publish firmware update to device: {}", 
                         device.getDeviceId(), e);
                throw new RuntimeException("Firmware update publication failed", e);
            }
        });
    }

    // Private helper methods

    private void connectToBroker() throws MqttException {
        mqttClient = new MqttClient(brokerUrl, clientId);
        
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setKeepAliveInterval(keepAlive);
        options.setConnectionTimeout(connectionTimeout);
        options.setAutomaticReconnect(true);
        
        if (!username.isEmpty()) {
            options.setUserName(username);
            options.setPassword(password.toCharArray());
        }
        
        mqttClient.connect(options);
        log.info("Connected to MQTT broker: {}", brokerUrl);
    }

    private void setupCallbacks() {
        mqttClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                log.error("MQTT connection lost", cause);
                // Automatic reconnection is enabled, but we log the event
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                try {
                    handleIncomingMessage(topic, message);
                } catch (Exception e) {
                    log.error("Error handling MQTT message from topic: {}", topic, e);
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                // Message delivery confirmation
                log.debug("Message delivery complete for topic: {}", 
                         token.getTopics()[0]);
            }
        });
    }

    private void handleIncomingMessage(String topic, MqttMessage message) {
        try {
            IoTDevice device = subscribedDevices.get(topic);
            if (device == null) {
                log.warn("Received message from unknown topic: {}", topic);
                return;
            }

            String payload = new String(message.getPayload());
            log.debug("Received message from device: {} on topic: {}, payload: {}", 
                     device.getDeviceId(), topic, payload);

            // Parse message based on device type and configuration
            if (topic.endsWith("/data")) {
                handleSensorData(device, payload);
            } else if (topic.endsWith("/status")) {
                handleDeviceStatus(device, payload);
            } else if (topic.endsWith("/heartbeat")) {
                handleHeartbeat(device, payload);
            } else if (topic.endsWith("/alerts")) {
                handleDeviceAlert(device, payload);
            } else {
                log.debug("Unhandled message type from topic: {}", topic);
            }

        } catch (Exception e) {
            log.error("Error processing MQTT message from topic: {}", topic, e);
        }
    }

    private void handleSensorData(IoTDevice device, String payload) {
        try {
            // Parse JSON payload to sensor reading
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            
            SensorReading reading = new SensorReading();
            reading.setDevice(device);
            reading.setReadingTimestamp(LocalDateTime.now());
            
            // Extract reading type and value
            if (data.containsKey("type")) {
                String readingType = (String) data.get("type");
                reading.setReadingType(SensorReading.ReadingType.valueOf(readingType.toUpperCase()));
            }
            
            if (data.containsKey("value")) {
                reading.setValue(data.get("value"));
            }
            
            if (data.containsKey("unit")) {
                reading.setUnit((String) data.get("unit"));
            }
            
            if (data.containsKey("sensor")) {
                reading.setSensorName((String) data.get("sensor"));
            }
            
            if (data.containsKey("quality")) {
                reading.setQualityIndicator((Integer) data.get("quality"));
            }
            
            // Add metadata
            data.forEach((key, value) -> {
                if (!key.equals("type") && !key.equals("value") && 
                    !key.equals("unit") && !key.equals("sensor") && 
                    !key.equals("quality")) {
                    reading.addMetadata(key, value.toString());
                }
            });
            
            // Process asynchronously
            edgeComputingService.processSensorData(device.getDeviceId(), reading);
            
        } catch (Exception e) {
            log.error("Error parsing sensor data from device: {}", device.getDeviceId(), e);
        }
    }

    private void handleDeviceStatus(IoTDevice device, String payload) {
        try {
            Map<String, Object> status = objectMapper.readValue(payload, Map.class);
            
            // Update device status fields
            if (status.containsKey("battery")) {
                device.setBatteryLevel((Integer) status.get("battery"));
            }
            
            if (status.containsKey("signal")) {
                device.setSignalStrength((Integer) status.get("signal"));
            }
            
            if (status.containsKey("temperature")) {
                device.setTemperature(new java.math.BigDecimal(status.get("temperature").toString()));
            }
            
            if (status.containsKey("status")) {
                String statusStr = (String) status.get("status");
                device.setStatus(IoTDevice.DeviceStatus.valueOf(statusStr.toUpperCase()));
            }
            
            device.updateHeartbeat();
            // Would normally save to database here
            
        } catch (Exception e) {
            log.error("Error parsing device status from device: {}", device.getDeviceId(), e);
        }
    }

    private void handleHeartbeat(IoTDevice device, String payload) {
        device.updateHeartbeat();
        log.debug("Received heartbeat from device: {}", device.getDeviceId());
    }

    private void handleDeviceAlert(IoTDevice device, String payload) {
        try {
            Map<String, Object> alert = objectMapper.readValue(payload, Map.class);
            log.warn("Received alert from device: {}, alert: {}", device.getDeviceId(), alert);
            
            // Would create alert record and trigger notifications
            
        } catch (Exception e) {
            log.error("Error parsing device alert from device: {}", device.getDeviceId(), e);
        }
    }
}