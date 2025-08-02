package com.erp.system.edge.service;

import com.erp.system.edge.entity.IoTDevice;
import com.erp.system.edge.entity.SensorReading;
import com.erp.system.edge.repository.IoTDeviceRepository;
import com.erp.system.edge.repository.SensorReadingRepository;
import com.erp.system.edge.dto.DeviceMetricsDto;
import com.erp.system.edge.dto.RealTimeAnalyticsDto;
import com.erp.system.edge.dto.EdgeNodeStatusDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;

/**
 * Edge Computing Service
 * Manages IoT devices, processes sensor data, and provides edge analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EdgeComputingService {

    private final IoTDeviceRepository deviceRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final MqttGatewayService mqttGatewayService;
    private final StreamProcessingService streamProcessingService;
    private final EdgeAnalyticsService edgeAnalyticsService;
    private final DeviceManagementService deviceManagementService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Register a new IoT device in the edge network
     */
    public IoTDevice registerDevice(IoTDevice device) {
        log.info("Registering new IoT device: {}", device.getDeviceId());
        
        // Validate device configuration
        validateDeviceConfiguration(device);
        
        // Set initial status
        device.setStatus(IoTDevice.DeviceStatus.CONFIGURING);
        device.setInstallationDate(LocalDateTime.now());
        
        // Save device
        IoTDevice savedDevice = deviceRepository.save(device);
        
        // Initialize device communication
        initializeDeviceCommunication(savedDevice);
        
        // Cache device info
        cacheDeviceInfo(savedDevice);
        
        // Publish device registration event
        publishDeviceEvent("device.registered", savedDevice);
        
        log.info("Successfully registered device: {}", savedDevice.getDeviceId());
        return savedDevice;
    }

    /**
     * Process incoming sensor data from IoT devices
     */
    @Async
    public CompletableFuture<Void> processSensorData(String deviceId, SensorReading reading) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("Processing sensor data from device: {}", deviceId);
                
                // Find device
                IoTDevice device = deviceRepository.findByDeviceId(deviceId)
                    .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));
                
                // Update device heartbeat
                device.updateHeartbeat();
                deviceRepository.save(device);
                
                // Set device reference
                reading.setDevice(device);
                reading.setReadingTimestamp(LocalDateTime.now());
                
                // Validate reading
                validateSensorReading(reading);
                
                // Real-time anomaly detection
                detectAnomalies(reading);
                
                // Save reading
                SensorReading savedReading = sensorReadingRepository.save(reading);
                
                // Real-time stream processing
                streamProcessingService.processReading(savedReading);
                
                // Update device metrics in cache
                updateDeviceMetrics(device, savedReading);
                
                // Check for alerts
                checkAlertConditions(device, savedReading);
                
                // Publish to real-time analytics
                publishToAnalyticsStream(savedReading);
                
                log.debug("Successfully processed sensor data from device: {}", deviceId);
                
            } catch (Exception e) {
                log.error("Error processing sensor data from device: {}", deviceId, e);
                throw new RuntimeException("Failed to process sensor data", e);
            }
        });
    }

    /**
     * Get real-time device metrics
     */
    @Transactional(readOnly = true)
    public DeviceMetricsDto getDeviceMetrics(String deviceId) {
        log.debug("Getting metrics for device: {}", deviceId);
        
        IoTDevice device = deviceRepository.findByDeviceId(deviceId)
            .orElseThrow(() -> new RuntimeException("Device not found: " + deviceId));
        
        // Get cached metrics first
        DeviceMetricsDto cachedMetrics = getCachedDeviceMetrics(deviceId);
        if (cachedMetrics != null) {
            return cachedMetrics;
        }
        
        // Calculate metrics from database
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<SensorReading> recentReadings = sensorReadingRepository
            .findByDeviceAndReadingTimestampAfterOrderByReadingTimestampDesc(device, since);
        
        DeviceMetricsDto metrics = DeviceMetricsDto.builder()
            .deviceId(deviceId)
            .deviceName(device.getDeviceName())
            .status(device.getStatus().name())
            .isOnline(device.isOnline())
            .lastHeartbeat(device.getLastHeartbeat())
            .batteryLevel(device.getBatteryLevel())
            .signalStrength(device.getSignalStrength())
            .totalReadings((long) recentReadings.size())
            .lastReading(recentReadings.isEmpty() ? null : recentReadings.get(0).getReadingTimestamp())
            .averageReadingRate(calculateReadingRate(recentReadings))
            .errorRate(calculateErrorRate(recentReadings))
            .build();
        
        // Cache metrics
        cacheDeviceMetrics(deviceId, metrics);
        
        return metrics;
    }

    /**
     * Get real-time analytics data
     */
    @Transactional(readOnly = true)
    public RealTimeAnalyticsDto getRealTimeAnalytics() {
        log.debug("Getting real-time analytics");
        
        // Get from cache first
        RealTimeAnalyticsDto cached = (RealTimeAnalyticsDto) redisTemplate
            .opsForValue().get("analytics:realtime");
        if (cached != null) {
            return cached;
        }
        
        // Calculate analytics
        LocalDateTime since = LocalDateTime.now().minusMinutes(30);
        
        long totalDevices = deviceRepository.count();
        long activeDevices = deviceRepository.countByStatus(IoTDevice.DeviceStatus.ACTIVE);
        long offlineDevices = deviceRepository.countByStatus(IoTDevice.DeviceStatus.OFFLINE);
        long errorDevices = deviceRepository.countByStatus(IoTDevice.DeviceStatus.ERROR);
        
        List<SensorReading> recentReadings = sensorReadingRepository
            .findByReadingTimestampAfter(since);
        
        long totalReadings = recentReadings.size();
        long anomalousReadings = recentReadings.stream()
            .mapToLong(r -> r.isAnomalous() ? 1 : 0)
            .sum();
        
        Map<String, Long> readingsByType = recentReadings.stream()
            .collect(Collectors.groupingBy(
                r -> r.getReadingType().name(),
                Collectors.counting()
            ));
        
        RealTimeAnalyticsDto analytics = RealTimeAnalyticsDto.builder()
            .timestamp(LocalDateTime.now())
            .totalDevices(totalDevices)
            .activeDevices(activeDevices)
            .offlineDevices(offlineDevices)
            .errorDevices(errorDevices)
            .totalReadings(totalReadings)
            .anomalousReadings(anomalousReadings)
            .readingsByType(readingsByType)
            .systemHealth(calculateSystemHealth(activeDevices, totalDevices))
            .build();
        
        // Cache for 1 minute
        redisTemplate.opsForValue().set("analytics:realtime", analytics);
        
        return analytics;
    }

    /**
     * Get edge node status
     */
    public List<EdgeNodeStatusDto> getEdgeNodeStatus() {
        log.debug("Getting edge node status");
        
        Map<String, List<IoTDevice>> devicesByNode = deviceRepository.findAll()
            .stream()
            .filter(device -> device.getEdgeNodeId() != null)
            .collect(Collectors.groupingBy(IoTDevice::getEdgeNodeId));
        
        return devicesByNode.entrySet().stream()
            .map(entry -> {
                String nodeId = entry.getKey();
                List<IoTDevice> devices = entry.getValue();
                
                long activeDevices = devices.stream()
                    .mapToLong(d -> d.getStatus() == IoTDevice.DeviceStatus.ACTIVE ? 1 : 0)
                    .sum();
                
                return EdgeNodeStatusDto.builder()
                    .nodeId(nodeId)
                    .totalDevices((long) devices.size())
                    .activeDevices(activeDevices)
                    .lastUpdate(LocalDateTime.now())
                    .health(calculateNodeHealth(activeDevices, devices.size()))
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Perform predictive maintenance analysis
     */
    @Async
    public CompletableFuture<Void> performPredictiveMaintenance() {
        return CompletableFuture.runAsync(() -> {
            log.info("Performing predictive maintenance analysis");
            
            List<IoTDevice> devices = deviceRepository.findAll();
            
            for (IoTDevice device : devices) {
                try {
                    // Analyze device health
                    edgeAnalyticsService.analyzeDeviceHealth(device);
                    
                    // Predict maintenance needs
                    edgeAnalyticsService.predictMaintenanceNeeds(device);
                    
                    // Update maintenance schedule if needed
                    deviceManagementService.updateMaintenanceSchedule(device);
                    
                } catch (Exception e) {
                    log.error("Error in predictive maintenance for device: {}", 
                             device.getDeviceId(), e);
                }
            }
            
            log.info("Completed predictive maintenance analysis");
        });
    }

    // Private helper methods
    
    private void validateDeviceConfiguration(IoTDevice device) {
        if (device.getDeviceId() == null || device.getDeviceId().trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID is required");
        }
        
        if (deviceRepository.findByDeviceId(device.getDeviceId()).isPresent()) {
            throw new IllegalArgumentException("Device ID already exists: " + device.getDeviceId());
        }
        
        if (device.getProtocol() == null) {
            throw new IllegalArgumentException("Communication protocol is required");
        }
    }

    private void initializeDeviceCommunication(IoTDevice device) {
        switch (device.getProtocol()) {
            case MQTT:
                mqttGatewayService.subscribeToDevice(device);
                break;
            case HTTP:
            case HTTPS:
                // Initialize HTTP polling if needed
                break;
            case COAP:
                // Initialize CoAP communication
                break;
            default:
                log.warn("Unsupported protocol for device {}: {}", 
                        device.getDeviceId(), device.getProtocol());
        }
        
        device.setStatus(IoTDevice.DeviceStatus.ACTIVE);
        deviceRepository.save(device);
    }

    private void validateSensorReading(SensorReading reading) {
        if (reading.getReadingType() == null) {
            throw new IllegalArgumentException("Reading type is required");
        }
        
        if (reading.getValue() == null) {
            throw new IllegalArgumentException("Reading value is required");
        }
    }

    private void detectAnomalies(SensorReading reading) {
        // Simple anomaly detection based on statistical analysis
        BigDecimal anomalyScore = edgeAnalyticsService.calculateAnomalyScore(reading);
        reading.setAnomalyScore(anomalyScore);
        
        if (reading.isAnomalous()) {
            reading.setAlertTriggered(true);
            log.warn("Anomaly detected in reading from device: {}, score: {}", 
                    reading.getDevice().getDeviceId(), anomalyScore);
        }
    }

    private void updateDeviceMetrics(IoTDevice device, SensorReading reading) {
        String key = "device:metrics:" + device.getDeviceId();
        redisTemplate.opsForHash().increment(key, "reading_count", 1);
        redisTemplate.opsForHash().put(key, "last_reading", reading.getReadingTimestamp());
    }

    private void checkAlertConditions(IoTDevice device, SensorReading reading) {
        // Check threshold-based alerts
        if (reading.isAnomalous() || device.isLowBattery() || device.hasWeakSignal()) {
            // Trigger alert
            deviceManagementService.createAlert(device, reading);
        }
    }

    private void publishToAnalyticsStream(SensorReading reading) {
        kafkaTemplate.send("sensor-readings", reading.getDevice().getDeviceId(), reading);
    }

    private void publishDeviceEvent(String eventType, IoTDevice device) {
        Map<String, Object> event = Map.of(
            "eventType", eventType,
            "deviceId", device.getDeviceId(),
            "timestamp", LocalDateTime.now(),
            "device", device
        );
        kafkaTemplate.send("device-events", device.getDeviceId(), event);
    }

    private double calculateReadingRate(List<SensorReading> readings) {
        if (readings.size() < 2) return 0.0;
        
        LocalDateTime first = readings.get(readings.size() - 1).getReadingTimestamp();
        LocalDateTime last = readings.get(0).getReadingTimestamp();
        
        long minutes = java.time.Duration.between(first, last).toMinutes();
        return minutes > 0 ? (double) readings.size() / minutes : 0.0;
    }

    private double calculateErrorRate(List<SensorReading> readings) {
        if (readings.isEmpty()) return 0.0;
        
        long errorCount = readings.stream()
            .mapToLong(r -> r.getQualityIndicator() < 50 ? 1 : 0)
            .sum();
        
        return (double) errorCount / readings.size() * 100;
    }

    private double calculateSystemHealth(long activeDevices, long totalDevices) {
        if (totalDevices == 0) return 100.0;
        return (double) activeDevices / totalDevices * 100;
    }

    private double calculateNodeHealth(long activeDevices, int totalDevices) {
        if (totalDevices == 0) return 100.0;
        return (double) activeDevices / totalDevices * 100;
    }

    private void cacheDeviceInfo(IoTDevice device) {
        String key = "device:info:" + device.getDeviceId();
        redisTemplate.opsForValue().set(key, device);
    }

    private DeviceMetricsDto getCachedDeviceMetrics(String deviceId) {
        String key = "device:metrics:" + deviceId;
        return (DeviceMetricsDto) redisTemplate.opsForValue().get(key);
    }

    private void cacheDeviceMetrics(String deviceId, DeviceMetricsDto metrics) {
        String key = "device:metrics:" + deviceId;
        redisTemplate.opsForValue().set(key, metrics);
    }
}