package com.erp.system.edge.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Sensor Reading Entity
 * Stores time series data from IoT sensors with optimized structure for analytics
 */
@Entity
@Table(name = "sensor_readings", indexes = {
    @Index(name = "idx_device_timestamp", columnList = "device_id, reading_timestamp"),
    @Index(name = "idx_reading_type_timestamp", columnList = "reading_type, reading_timestamp"),
    @Index(name = "idx_timestamp_desc", columnList = "reading_timestamp DESC")
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    @NotNull(message = "Device is required")
    private IoTDevice device;

    @Column(name = "reading_timestamp", nullable = false)
    @NotNull(message = "Reading timestamp is required")
    private LocalDateTime readingTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_type", nullable = false, length = 50)
    @NotNull(message = "Reading type is required")
    private ReadingType readingType;

    @Column(name = "sensor_name", length = 100)
    private String sensorName;

    @Column(name = "numeric_value", precision = 19, scale = 6)
    private BigDecimal numericValue;

    @Column(name = "string_value", length = 500)
    private String stringValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "quality_indicator")
    private Integer qualityIndicator; // 0-100, 100 being highest quality

    @Column(name = "accuracy", precision = 5, scale = 2)
    private BigDecimal accuracy;

    @Column(name = "precision_value", precision = 5, scale = 2)
    private BigDecimal precision;

    @ElementCollection
    @CollectionTable(name = "reading_metadata", 
                    joinColumns = @JoinColumn(name = "reading_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();

    @Column(name = "location_lat", precision = 10, scale = 8)
    private BigDecimal locationLatitude;

    @Column(name = "location_lng", precision = 11, scale = 8)
    private BigDecimal locationLongitude;

    @Column(name = "processed", nullable = false)
    private Boolean processed = false;

    @Column(name = "anomaly_score", precision = 5, scale = 4)
    private BigDecimal anomalyScore;

    @Column(name = "prediction_confidence", precision = 5, scale = 4)
    private BigDecimal predictionConfidence;

    @Column(name = "alert_triggered", nullable = false)
    private Boolean alertTriggered = false;

    @Column(name = "batch_id")
    private String batchId;

    @Column(name = "sequence_number")
    private Long sequenceNumber;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "raw_data", columnDefinition = "TEXT")
    private String rawData;

    @Column(name = "checksum")
    private String checksum;

    @Column(name = "encrypted", nullable = false)
    private Boolean encrypted = false;

    /**
     * Types of sensor readings supported
     */
    public enum ReadingType {
        TEMPERATURE,
        HUMIDITY,
        PRESSURE,
        MOTION,
        LIGHT,
        SOUND,
        VIBRATION,
        ACCELERATION,
        PROXIMITY,
        MAGNETIC_FIELD,
        ELECTRIC_CURRENT,
        VOLTAGE,
        POWER,
        ENERGY,
        FLOW_RATE,
        LEVEL,
        PH,
        CONDUCTIVITY,
        TURBIDITY,
        DISSOLVED_OXYGEN,
        GPS_LOCATION,
        RFID_TAG,
        BARCODE,
        QR_CODE,
        IMAGE,
        VIDEO,
        AUDIO,
        GAS_CONCENTRATION,
        PARTICLE_COUNT,
        RADIATION,
        WEIGHT,
        FORCE,
        TORQUE,
        SPEED,
        RPM,
        ANGLE,
        DISTANCE,
        COUNT,
        STATUS,
        ERROR_CODE,
        DIAGNOSTIC,
        HEARTBEAT,
        CUSTOM
    }

    // Business methods
    public Object getValue() {
        if (numericValue != null) return numericValue;
        if (stringValue != null) return stringValue;
        if (booleanValue != null) return booleanValue;
        return null;
    }

    public void setValue(Object value) {
        if (value instanceof Number) {
            this.numericValue = new BigDecimal(value.toString());
        } else if (value instanceof Boolean) {
            this.booleanValue = (Boolean) value;
        } else if (value != null) {
            this.stringValue = value.toString();
        }
    }

    public boolean isNumeric() {
        return numericValue != null;
    }

    public boolean isString() {
        return stringValue != null;
    }

    public boolean isBoolean() {
        return booleanValue != null;
    }

    public boolean isHighQuality() {
        return qualityIndicator != null && qualityIndicator >= 80;
    }

    public boolean isAnomalous() {
        return anomalyScore != null && anomalyScore.compareTo(new BigDecimal("0.8")) > 0;
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    public String getMetadata(String key) {
        return this.metadata.get(key);
    }

    public boolean hasMetadata(String key) {
        return this.metadata.containsKey(key);
    }

    @PrePersist
    public void prePersist() {
        if (readingTimestamp == null) {
            readingTimestamp = LocalDateTime.now();
        }
        if (qualityIndicator == null) {
            qualityIndicator = 100; // Default to highest quality
        }
    }

    @PostPersist
    public void postPersist() {
        // Trigger real-time processing if needed
        if (alertTriggered) {
            // Could trigger events here
        }
    }
}