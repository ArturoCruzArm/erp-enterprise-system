package com.erp.system.edge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IoT Device Entity
 * Represents physical IoT devices connected to the edge computing network
 */
@Entity
@Table(name = "iot_devices", indexes = {
    @Index(name = "idx_device_type_status", columnList = "device_type, status"),
    @Index(name = "idx_location_facility", columnList = "location_id, facility_id"),
    @Index(name = "idx_last_heartbeat", columnList = "last_heartbeat")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class IoTDevice extends BaseEntity {

    @NotBlank(message = "Device ID is required")
    @Column(name = "device_id", unique = true, nullable = false, length = 100)
    private String deviceId;

    @NotBlank(message = "Device name is required")
    @Column(name = "device_name", nullable = false, length = 200)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Device type is required")
    @Column(name = "device_type", nullable = false, length = 50)
    private DeviceType deviceType;

    @NotBlank(message = "Manufacturer is required")
    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer;

    @NotBlank(message = "Model is required")
    @Column(name = "model", nullable = false, length = 100)
    private String model;

    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    @Column(name = "hardware_version", length = 50)
    private String hardwareVersion;

    @Column(name = "mac_address", length = 17)
    private String macAddress;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "protocol", nullable = false, length = 20)
    private CommunicationProtocol protocol;

    @Column(name = "endpoint_url", length = 500)
    private String endpointUrl;

    @Column(name = "mqtt_topic", length = 200)
    private String mqttTopic;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeviceStatus status = DeviceStatus.INACTIVE;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "facility_id")
    private Long facilityId;

    @Column(name = "latitude", precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "altitude", precision = 8, scale = 2)
    private BigDecimal altitude;

    @Column(name = "installation_date")
    private LocalDateTime installationDate;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @Column(name = "last_maintenance")
    private LocalDateTime lastMaintenance;

    @Column(name = "next_maintenance")
    private LocalDateTime nextMaintenance;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "signal_strength")
    private Integer signalStrength;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "humidity", precision = 5, scale = 2)
    private BigDecimal humidity;

    @ElementCollection
    @CollectionTable(name = "device_capabilities", 
                    joinColumns = @JoinColumn(name = "device_id"))
    @MapKeyColumn(name = "capability_name")
    @Column(name = "capability_value")
    private Map<String, String> capabilities = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "device_configuration", 
                    joinColumns = @JoinColumn(name = "device_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value")
    private Map<String, String> configuration = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "device_tags", 
                    joinColumns = @JoinColumn(name = "device_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SensorReading> sensorReadings = new ArrayList<>();

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DeviceAlert> alerts = new ArrayList<>();

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaintenanceLog> maintenanceLogs = new ArrayList<>();

    @Column(name = "edge_node_id")
    private String edgeNodeId;

    @Column(name = "is_critical", nullable = false)
    private Boolean isCritical = false;

    @Column(name = "encryption_enabled", nullable = false)
    private Boolean encryptionEnabled = true;

    @Column(name = "data_retention_days")
    private Integer dataRetentionDays = 365;

    @Column(name = "sampling_rate_ms")
    private Integer samplingRateMs = 1000;

    @Column(name = "alert_threshold_config", columnDefinition = "TEXT")
    private String alertThresholdConfig;

    @Column(name = "ml_model_config", columnDefinition = "TEXT")
    private String mlModelConfig;

    /**
     * Device types supported by the edge computing platform
     */
    public enum DeviceType {
        SENSOR, 
        ACTUATOR, 
        GATEWAY, 
        CAMERA, 
        BEACON, 
        CONTROLLER, 
        METER, 
        SCANNER, 
        MONITOR, 
        ROBOT, 
        DRONE, 
        VEHICLE,
        ENVIRONMENTAL,
        SECURITY,
        INDUSTRIAL,
        WEARABLE
    }

    /**
     * Communication protocols supported
     */
    public enum CommunicationProtocol {
        MQTT, 
        COAP, 
        HTTP, 
        HTTPS, 
        WEBSOCKET, 
        MODBUS, 
        OPCUA, 
        ZIGBEE, 
        BLUETOOTH, 
        WIFI, 
        ETHERNET, 
        LORA, 
        CELLULAR,
        NFC,
        RFID
    }

    /**
     * Device operational status
     */
    public enum DeviceStatus {
        ACTIVE, 
        INACTIVE, 
        MAINTENANCE, 
        ERROR, 
        OFFLINE, 
        CONFIGURING, 
        UPDATING, 
        DECOMMISSIONED,
        SLEEPING,
        CRITICAL_ERROR
    }

    // Business methods
    public boolean isOnline() {
        return status == DeviceStatus.ACTIVE && 
               lastHeartbeat != null && 
               lastHeartbeat.isAfter(LocalDateTime.now().minusMinutes(5));
    }

    public boolean requiresMaintenance() {
        return nextMaintenance != null && 
               nextMaintenance.isBefore(LocalDateTime.now());
    }

    public boolean isLowBattery() {
        return batteryLevel != null && batteryLevel < 20;
    }

    public boolean hasWeakSignal() {
        return signalStrength != null && signalStrength < -80;
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
        if (this.status == DeviceStatus.OFFLINE) {
            this.status = DeviceStatus.ACTIVE;
        }
    }

    public void addCapability(String name, String value) {
        this.capabilities.put(name, value);
    }

    public void setConfiguration(String key, String value) {
        this.configuration.put(key, value);
    }

    public String getConfiguration(String key) {
        return this.configuration.get(key);
    }

    public void addTag(String tag) {
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }

    public boolean hasTag(String tag) {
        return this.tags.contains(tag);
    }

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (installationDate == null) {
            installationDate = LocalDateTime.now();
        }
    }
}