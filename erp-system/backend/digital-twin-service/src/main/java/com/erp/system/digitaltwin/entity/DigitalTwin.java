package com.erp.system.digitaltwin.entity;

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
 * Digital Twin Entity
 * Represents a digital replica of a physical asset or system
 */
@Entity
@Table(name = "digital_twins", indexes = {
    @Index(name = "idx_twin_type_status", columnList = "twin_type, status"),
    @Index(name = "idx_physical_asset", columnList = "physical_asset_id"),
    @Index(name = "idx_last_sync", columnList = "last_sync_timestamp")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DigitalTwin extends BaseEntity {

    @NotBlank(message = "Twin ID is required")
    @Column(name = "twin_id", unique = true, nullable = false, length = 100)
    private String twinId;

    @NotBlank(message = "Twin name is required")
    @Column(name = "twin_name", nullable = false, length = 200)
    private String twinName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Twin type is required")
    @Column(name = "twin_type", nullable = false, length = 50)
    private TwinType twinType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TwinStatus status = TwinStatus.INACTIVE;

    @Column(name = "physical_asset_id", length = 100)
    private String physicalAssetId;

    @Column(name = "physical_asset_type", length = 50)
    private String physicalAssetType;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "facility_id")
    private Long facilityId;

    @Column(name = "department_id")
    private Long departmentId;

    // 3D Model and Visualization
    @Column(name = "model_file_path", length = 500)
    private String modelFilePath;

    @Column(name = "model_format", length = 20)
    private String modelFormat; // GLTF, OBJ, FBX, etc.

    @Column(name = "texture_files", columnDefinition = "TEXT")
    private String textureFiles; // JSON array of texture file paths

    @Column(name = "scale_factor", precision = 10, scale = 6)
    private BigDecimal scaleFactor = BigDecimal.ONE;

    // Spatial Positioning
    @Column(name = "position_x", precision = 15, scale = 6)
    private BigDecimal positionX = BigDecimal.ZERO;

    @Column(name = "position_y", precision = 15, scale = 6)
    private BigDecimal positionY = BigDecimal.ZERO;

    @Column(name = "position_z", precision = 15, scale = 6)
    private BigDecimal positionZ = BigDecimal.ZERO;

    @Column(name = "rotation_x", precision = 8, scale = 4)
    private BigDecimal rotationX = BigDecimal.ZERO;

    @Column(name = "rotation_y", precision = 8, scale = 4)
    private BigDecimal rotationY = BigDecimal.ZERO;

    @Column(name = "rotation_z", precision = 8, scale = 4)
    private BigDecimal rotationZ = BigDecimal.ZERO;

    // Dimensions
    @Column(name = "width", precision = 10, scale = 3)
    private BigDecimal width;

    @Column(name = "height", precision = 10, scale = 3)
    private BigDecimal height;

    @Column(name = "depth", precision = 10, scale = 3)
    private BigDecimal depth;

    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;

    // Synchronization
    @Column(name = "sync_frequency_seconds")
    private Integer syncFrequencySeconds = 60;

    @Column(name = "last_sync_timestamp")
    private LocalDateTime lastSyncTimestamp;

    @Column(name = "sync_status", length = 20)
    private String syncStatus = "PENDING";

    @Column(name = "data_source_config", columnDefinition = "TEXT")
    private String dataSourceConfig; // JSON configuration for data sources

    // Properties and Configuration
    @ElementCollection
    @CollectionTable(name = "twin_properties", 
                    joinColumns = @JoinColumn(name = "twin_id"))
    @MapKeyColumn(name = "property_name")
    @Column(name = "property_value")
    private Map<String, String> properties = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "twin_configuration", 
                    joinColumns = @JoinColumn(name = "twin_id"))
    @MapKeyColumn(name = "config_key")
    @Column(name = "config_value")
    private Map<String, String> configuration = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "twin_tags", 
                    joinColumns = @JoinColumn(name = "twin_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    // Relationships
    @OneToMany(mappedBy = "digitalTwin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TwinSensorData> sensorData = new ArrayList<>();

    @OneToMany(mappedBy = "digitalTwin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SimulationRun> simulationRuns = new ArrayList<>();

    @OneToMany(mappedBy = "digitalTwin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TwinEvent> events = new ArrayList<>();

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DigitalTwin> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_twin_id")
    private DigitalTwin parent;

    // Analytics and Performance
    @Column(name = "performance_score", precision = 5, scale = 2)
    private BigDecimal performanceScore;

    @Column(name = "efficiency_rating", precision = 5, scale = 2)
    private BigDecimal efficiencyRating;

    @Column(name = "health_status", length = 20)
    private String healthStatus = "UNKNOWN";

    @Column(name = "uptime_percentage", precision = 5, scale = 2)
    private BigDecimal uptimePercentage;

    @Column(name = "maintenance_score", precision = 5, scale = 2)
    private BigDecimal maintenanceScore;

    // Simulation Parameters
    @Column(name = "physics_enabled", nullable = false)
    private Boolean physicsEnabled = false;

    @Column(name = "collision_detection", nullable = false)
    private Boolean collisionDetection = false;

    @Column(name = "gravity_enabled", nullable = false)
    private Boolean gravityEnabled = false;

    @Column(name = "simulation_precision", length = 20)
    private String simulationPrecision = "MEDIUM";

    // Machine Learning
    @Column(name = "ml_model_id")
    private String mlModelId;

    @Column(name = "prediction_accuracy", precision = 5, scale = 4)
    private BigDecimal predictionAccuracy;

    @Column(name = "anomaly_threshold", precision = 5, scale = 4)
    private BigDecimal anomalyThreshold = new BigDecimal("0.8");

    // Lifecycle Management
    @Column(name = "manufacture_date")
    private LocalDateTime manufactureDate;

    @Column(name = "installation_date")
    private LocalDateTime installationDate;

    @Column(name = "warranty_expiry")
    private LocalDateTime warrantyExpiry;

    @Column(name = "planned_retirement")
    private LocalDateTime plannedRetirement;

    @Column(name = "operating_hours", precision = 15, scale = 2)
    private BigDecimal operatingHours = BigDecimal.ZERO;

    @Column(name = "maintenance_hours", precision = 15, scale = 2)
    private BigDecimal maintenanceHours = BigDecimal.ZERO;

    /**
     * Types of digital twins
     */
    public enum TwinType {
        ASSET,           // Physical asset twin
        PROCESS,         // Process twin
        SYSTEM,          // System-level twin
        PRODUCT,         // Product twin
        FACILITY,        // Facility twin
        SUPPLY_CHAIN,    // Supply chain twin
        HUMAN,           // Human/worker twin
        ENVIRONMENTAL,   // Environmental twin
        NETWORK,         // Network infrastructure twin
        COMPOSITE        // Composite twin (multiple sub-twins)
    }

    /**
     * Digital twin operational status
     */
    public enum TwinStatus {
        ACTIVE,
        INACTIVE,
        SIMULATION,
        MAINTENANCE,
        ERROR,
        CALIBRATING,
        LEARNING,
        PREDICTING,
        DECOMMISSIONED
    }

    // Business methods
    public boolean isActive() {
        return status == TwinStatus.ACTIVE;
    }

    public boolean isSyncRequired() {
        if (lastSyncTimestamp == null) return true;
        
        LocalDateTime nextSync = lastSyncTimestamp.plusSeconds(syncFrequencySeconds);
        return LocalDateTime.now().isAfter(nextSync);
    }

    public boolean isHealthy() {
        return "HEALTHY".equals(healthStatus) || "GOOD".equals(healthStatus);
    }

    public boolean hasHighPerformance() {
        return performanceScore != null && 
               performanceScore.compareTo(new BigDecimal("80")) >= 0;
    }

    public void updateSyncStatus() {
        this.lastSyncTimestamp = LocalDateTime.now();
        this.syncStatus = "COMPLETED";
    }

    public void addProperty(String name, String value) {
        this.properties.put(name, value);
    }

    public String getProperty(String name) {
        return this.properties.get(name);
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

    public void addChild(DigitalTwin child) {
        this.children.add(child);
        child.setParent(this);
    }

    public void removeChild(DigitalTwin child) {
        this.children.remove(child);
        child.setParent(null);
    }

    public boolean isComposite() {
        return twinType == TwinType.COMPOSITE && !children.isEmpty();
    }

    public void updateOperatingHours(BigDecimal hours) {
        if (this.operatingHours == null) {
            this.operatingHours = BigDecimal.ZERO;
        }
        this.operatingHours = this.operatingHours.add(hours);
    }

    public void updateMaintenanceHours(BigDecimal hours) {
        if (this.maintenanceHours == null) {
            this.maintenanceHours = BigDecimal.ZERO;
        }
        this.maintenanceHours = this.maintenanceHours.add(hours);
    }

    public BigDecimal calculateAge() {
        if (installationDate == null) return BigDecimal.ZERO;
        
        long days = java.time.Duration.between(
            installationDate.atZone(java.time.ZoneId.systemDefault()).toInstant(),
            LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()
        ).toDays();
        
        return new BigDecimal(days);
    }

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (installationDate == null) {
            installationDate = LocalDateTime.now();
        }
    }
}