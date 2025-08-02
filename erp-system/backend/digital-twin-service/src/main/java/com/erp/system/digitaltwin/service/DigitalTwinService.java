package com.erp.system.digitaltwin.service;

import com.erp.system.digitaltwin.entity.DigitalTwin;
import com.erp.system.digitaltwin.entity.TwinSensorData;
import com.erp.system.digitaltwin.entity.SimulationRun;
import com.erp.system.digitaltwin.repository.DigitalTwinRepository;
import com.erp.system.digitaltwin.repository.TwinSensorDataRepository;
import com.erp.system.digitaltwin.repository.SimulationRunRepository;
import com.erp.system.digitaltwin.dto.TwinCreateDto;
import com.erp.system.digitaltwin.dto.TwinUpdateDto;
import com.erp.system.digitaltwin.dto.TwinAnalyticsDto;
import com.erp.system.digitaltwin.dto.SimulationResultDto;

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
 * Digital Twin Service
 * Manages digital twin lifecycle, synchronization, and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DigitalTwinService {

    private final DigitalTwinRepository digitalTwinRepository;
    private final TwinSensorDataRepository sensorDataRepository;
    private final SimulationRunRepository simulationRunRepository;
    private final TwinSynchronizationService synchronizationService;
    private final SimulationEngineService simulationEngineService;
    private final TwinAnalyticsService analyticsService;
    private final ModelManagementService modelManagementService;
    private final PhysicsEngineService physicsEngineService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Create a new digital twin
     */
    public DigitalTwin createDigitalTwin(TwinCreateDto createDto) {
        log.info("Creating new digital twin: {}", createDto.getTwinName());
        
        // Validate twin data
        validateTwinCreation(createDto);
        
        // Create twin entity
        DigitalTwin twin = DigitalTwin.builder()
            .twinId(generateTwinId(createDto))
            .twinName(createDto.getTwinName())
            .description(createDto.getDescription())
            .twinType(createDto.getTwinType())
            .physicalAssetId(createDto.getPhysicalAssetId())
            .physicalAssetType(createDto.getPhysicalAssetType())
            .locationId(createDto.getLocationId())
            .facilityId(createDto.getFacilityId())
            .status(DigitalTwin.TwinStatus.INACTIVE)
            .syncFrequencySeconds(createDto.getSyncFrequencySeconds())
            .physicsEnabled(createDto.getPhysicsEnabled())
            .collisionDetection(createDto.getCollisionDetection())
            .simulationPrecision(createDto.getSimulationPrecision())
            .build();
        
        // Set properties and configuration
        if (createDto.getProperties() != null) {
            createDto.getProperties().forEach(twin::addProperty);
        }
        
        if (createDto.getConfiguration() != null) {
            createDto.getConfiguration().forEach(twin::setConfiguration);
        }
        
        if (createDto.getTags() != null) {
            createDto.getTags().forEach(twin::addTag);
        }
        
        // Save twin
        DigitalTwin savedTwin = digitalTwinRepository.save(twin);
        
        // Initialize 3D model if provided
        if (createDto.getModelFilePath() != null) {
            initializeTwinModel(savedTwin, createDto);
        }
        
        // Setup data synchronization
        if (createDto.getPhysicalAssetId() != null) {
            synchronizationService.setupSynchronization(savedTwin);
        }
        
        // Cache twin info
        cacheTwinInfo(savedTwin);
        
        // Publish creation event
        publishTwinEvent("twin.created", savedTwin);
        
        log.info("Successfully created digital twin: {}", savedTwin.getTwinId());
        return savedTwin;
    }

    /**
     * Update existing digital twin
     */
    public DigitalTwin updateDigitalTwin(String twinId, TwinUpdateDto updateDto) {
        log.info("Updating digital twin: {}", twinId);
        
        DigitalTwin twin = digitalTwinRepository.findByTwinId(twinId)
            .orElseThrow(() -> new RuntimeException("Digital twin not found: " + twinId));
        
        // Update basic properties
        if (updateDto.getTwinName() != null) {
            twin.setTwinName(updateDto.getTwinName());
        }
        
        if (updateDto.getDescription() != null) {
            twin.setDescription(updateDto.getDescription());
        }
        
        if (updateDto.getStatus() != null) {
            twin.setStatus(updateDto.getStatus());
        }
        
        if (updateDto.getSyncFrequencySeconds() != null) {
            twin.setSyncFrequencySeconds(updateDto.getSyncFrequencySeconds());
        }
        
        // Update properties and configuration
        if (updateDto.getProperties() != null) {
            updateDto.getProperties().forEach(twin::addProperty);
        }
        
        if (updateDto.getConfiguration() != null) {
            updateDto.getConfiguration().forEach(twin::setConfiguration);
        }
        
        // Update positioning if provided
        if (updateDto.getPosition() != null) {
            twin.setPositionX(updateDto.getPosition().getX());
            twin.setPositionY(updateDto.getPosition().getY());
            twin.setPositionZ(updateDto.getPosition().getZ());
        }
        
        if (updateDto.getRotation() != null) {
            twin.setRotationX(updateDto.getRotation().getX());
            twin.setRotationY(updateDto.getRotation().getY());
            twin.setRotationZ(updateDto.getRotation().getZ());
        }
        
        // Save updated twin
        DigitalTwin savedTwin = digitalTwinRepository.save(twin);
        
        // Update cache
        cacheTwinInfo(savedTwin);
        
        // Publish update event
        publishTwinEvent("twin.updated", savedTwin);
        
        log.info("Successfully updated digital twin: {}", twinId);
        return savedTwin;
    }

    /**
     * Synchronize digital twin with physical asset
     */
    @Async
    public CompletableFuture<Void> synchronizeTwin(String twinId) {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("Synchronizing digital twin: {}", twinId);
                
                DigitalTwin twin = digitalTwinRepository.findByTwinId(twinId)
                    .orElseThrow(() -> new RuntimeException("Digital twin not found: " + twinId));
                
                // Perform synchronization
                synchronizationService.synchronize(twin);
                
                // Update sync status
                twin.updateSyncStatus();
                digitalTwinRepository.save(twin);
                
                // Update cache
                cacheTwinInfo(twin);
                
                // Publish sync event
                publishTwinEvent("twin.synchronized", twin);
                
                log.debug("Successfully synchronized digital twin: {}", twinId);
                
            } catch (Exception e) {
                log.error("Error synchronizing digital twin: {}", twinId, e);
                throw new RuntimeException("Synchronization failed", e);
            }
        });
    }

    /**
     * Run simulation on digital twin
     */
    @Async
    public CompletableFuture<SimulationResultDto> runSimulation(String twinId, Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Running simulation for digital twin: {}", twinId);
                
                DigitalTwin twin = digitalTwinRepository.findByTwinId(twinId)
                    .orElseThrow(() -> new RuntimeException("Digital twin not found: " + twinId));
                
                // Create simulation run record
                SimulationRun simulationRun = SimulationRun.builder()
                    .digitalTwin(twin)
                    .simulationType("PERFORMANCE_ANALYSIS")
                    .parameters(parameters.toString())
                    .status("RUNNING")
                    .startTime(LocalDateTime.now())
                    .build();
                
                simulationRun = simulationRunRepository.save(simulationRun);
                
                // Run simulation
                SimulationResultDto result = simulationEngineService.runSimulation(twin, parameters);
                
                // Update simulation run
                simulationRun.setStatus("COMPLETED");
                simulationRun.setEndTime(LocalDateTime.now());
                simulationRun.setResults(result.toString());
                simulationRunRepository.save(simulationRun);
                
                // Update twin analytics
                updateTwinAnalytics(twin, result);
                
                // Publish simulation completed event
                publishTwinEvent("simulation.completed", twin, Map.of("result", result));
                
                log.info("Successfully completed simulation for digital twin: {}", twinId);
                return result;
                
            } catch (Exception e) {
                log.error("Error running simulation for digital twin: {}", twinId, e);
                throw new RuntimeException("Simulation failed", e);
            }
        });
    }

    /**
     * Get digital twin analytics
     */
    @Transactional(readOnly = true)
    public TwinAnalyticsDto getTwinAnalytics(String twinId) {
        log.debug("Getting analytics for digital twin: {}", twinId);
        
        DigitalTwin twin = digitalTwinRepository.findByTwinId(twinId)
            .orElseThrow(() -> new RuntimeException("Digital twin not found: " + twinId));
        
        // Check cache first
        TwinAnalyticsDto cached = getCachedAnalytics(twinId);
        if (cached != null) {
            return cached;
        }
        
        // Calculate analytics
        TwinAnalyticsDto analytics = analyticsService.calculateAnalytics(twin);
        
        // Cache results
        cacheAnalytics(twinId, analytics);
        
        return analytics;
    }

    /**
     * Predict future behavior using machine learning
     */
    @Async
    public CompletableFuture<Map<String, Object>> predictBehavior(String twinId, int horizonDays) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Predicting behavior for digital twin: {} with horizon: {} days", 
                        twinId, horizonDays);
                
                DigitalTwin twin = digitalTwinRepository.findByTwinId(twinId)
                    .orElseThrow(() -> new RuntimeException("Digital twin not found: " + twinId));
                
                // Get historical data
                LocalDateTime since = LocalDateTime.now().minusDays(30);
                List<TwinSensorData> historicalData = sensorDataRepository
                    .findByDigitalTwinAndTimestampAfterOrderByTimestampAsc(twin, since);
                
                // Run prediction algorithm
                Map<String, Object> predictions = analyticsService.predictBehavior(twin, historicalData, horizonDays);
                
                // Update prediction accuracy if available
                if (predictions.containsKey("confidence")) {
                    BigDecimal confidence = new BigDecimal(predictions.get("confidence").toString());
                    twin.setPredictionAccuracy(confidence);
                    digitalTwinRepository.save(twin);
                }
                
                // Cache predictions
                String cacheKey = "predictions:" + twinId + ":" + horizonDays;
                redisTemplate.opsForValue().set(cacheKey, predictions);
                
                // Publish prediction event
                publishTwinEvent("prediction.completed", twin, predictions);
                
                log.info("Successfully predicted behavior for digital twin: {}", twinId);
                return predictions;
                
            } catch (Exception e) {
                log.error("Error predicting behavior for digital twin: {}", twinId, e);
                throw new RuntimeException("Prediction failed", e);
            }
        });
    }

    /**
     * Optimize twin performance
     */
    @Async
    public CompletableFuture<Map<String, Object>> optimizePerformance(String twinId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.info("Optimizing performance for digital twin: {}", twinId);
                
                DigitalTwin twin = digitalTwinRepository.findByTwinId(twinId)
                    .orElseThrow(() -> new RuntimeException("Digital twin not found: " + twinId));
                
                // Run optimization algorithms
                Map<String, Object> optimizations = analyticsService.optimizePerformance(twin);
                
                // Apply optimizations to twin configuration
                if (optimizations.containsKey("configuration")) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> newConfig = (Map<String, String>) optimizations.get("configuration");
                    newConfig.forEach(twin::setConfiguration);
                    digitalTwinRepository.save(twin);
                }
                
                // Update performance score
                if (optimizations.containsKey("performance_score")) {
                    BigDecimal score = new BigDecimal(optimizations.get("performance_score").toString());
                    twin.setPerformanceScore(score);
                    digitalTwinRepository.save(twin);
                }
                
                // Publish optimization event
                publishTwinEvent("optimization.completed", twin, optimizations);
                
                log.info("Successfully optimized performance for digital twin: {}", twinId);
                return optimizations;
                
            } catch (Exception e) {
                log.error("Error optimizing performance for digital twin: {}", twinId, e);
                throw new RuntimeException("Optimization failed", e);
            }
        });
    }

    /**
     * Get all digital twins with filtering
     */
    @Transactional(readOnly = true)
    public List<DigitalTwin> getAllTwins(DigitalTwin.TwinType twinType, 
                                        DigitalTwin.TwinStatus status,
                                        Long facilityId) {
        if (twinType != null && status != null && facilityId != null) {
            return digitalTwinRepository.findByTwinTypeAndStatusAndFacilityId(twinType, status, facilityId);
        } else if (twinType != null && status != null) {
            return digitalTwinRepository.findByTwinTypeAndStatus(twinType, status);
        } else if (twinType != null) {
            return digitalTwinRepository.findByTwinType(twinType);
        } else if (status != null) {
            return digitalTwinRepository.findByStatus(status);
        } else if (facilityId != null) {
            return digitalTwinRepository.findByFacilityId(facilityId);
        } else {
            return digitalTwinRepository.findAll();
        }
    }

    /**
     * Delete digital twin
     */
    public void deleteDigitalTwin(String twinId) {
        log.info("Deleting digital twin: {}", twinId);
        
        DigitalTwin twin = digitalTwinRepository.findByTwinId(twinId)
            .orElseThrow(() -> new RuntimeException("Digital twin not found: " + twinId));
        
        // Stop synchronization
        synchronizationService.stopSynchronization(twin);
        
        // Clean up model files
        modelManagementService.deleteModel(twin);
        
        // Remove from cache
        removeTwinFromCache(twinId);
        
        // Delete twin
        digitalTwinRepository.delete(twin);
        
        // Publish deletion event
        publishTwinEvent("twin.deleted", twin);
        
        log.info("Successfully deleted digital twin: {}", twinId);
    }

    // Private helper methods
    
    private void validateTwinCreation(TwinCreateDto createDto) {
        if (createDto.getTwinName() == null || createDto.getTwinName().trim().isEmpty()) {
            throw new IllegalArgumentException("Twin name is required");
        }
        
        if (createDto.getTwinType() == null) {
            throw new IllegalArgumentException("Twin type is required");
        }
        
        // Check for duplicate twin ID if provided
        if (createDto.getTwinId() != null) {
            if (digitalTwinRepository.findByTwinId(createDto.getTwinId()).isPresent()) {
                throw new IllegalArgumentException("Twin ID already exists: " + createDto.getTwinId());
            }
        }
    }

    private String generateTwinId(TwinCreateDto createDto) {
        if (createDto.getTwinId() != null) {
            return createDto.getTwinId();
        }
        
        String prefix = createDto.getTwinType().name().toLowerCase();
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "_" + timestamp;
    }

    private void initializeTwinModel(DigitalTwin twin, TwinCreateDto createDto) {
        twin.setModelFilePath(createDto.getModelFilePath());
        twin.setModelFormat(createDto.getModelFormat());
        twin.setScaleFactor(createDto.getScaleFactor());
        
        // Initialize physics if enabled
        if (twin.getPhysicsEnabled()) {
            physicsEngineService.initializePhysics(twin);
        }
        
        // Load and validate 3D model
        modelManagementService.loadModel(twin);
    }

    private void updateTwinAnalytics(DigitalTwin twin, SimulationResultDto result) {
        if (result.getPerformanceScore() != null) {
            twin.setPerformanceScore(result.getPerformanceScore());
        }
        
        if (result.getEfficiencyRating() != null) {
            twin.setEfficiencyRating(result.getEfficiencyRating());
        }
        
        if (result.getHealthStatus() != null) {
            twin.setHealthStatus(result.getHealthStatus());
        }
        
        digitalTwinRepository.save(twin);
    }

    private void cacheTwinInfo(DigitalTwin twin) {
        String key = "twin:info:" + twin.getTwinId();
        redisTemplate.opsForValue().set(key, twin);
    }

    private TwinAnalyticsDto getCachedAnalytics(String twinId) {
        String key = "twin:analytics:" + twinId;
        return (TwinAnalyticsDto) redisTemplate.opsForValue().get(key);
    }

    private void cacheAnalytics(String twinId, TwinAnalyticsDto analytics) {
        String key = "twin:analytics:" + twinId;
        redisTemplate.opsForValue().set(key, analytics);
    }

    private void removeTwinFromCache(String twinId) {
        redisTemplate.delete("twin:info:" + twinId);
        redisTemplate.delete("twin:analytics:" + twinId);
    }

    private void publishTwinEvent(String eventType, DigitalTwin twin) {
        publishTwinEvent(eventType, twin, Map.of());
    }

    private void publishTwinEvent(String eventType, DigitalTwin twin, Map<String, Object> data) {
        Map<String, Object> event = new HashMap<>(data);
        event.put("eventType", eventType);
        event.put("twinId", twin.getTwinId());
        event.put("timestamp", LocalDateTime.now());
        event.put("twin", twin);
        
        kafkaTemplate.send("digital-twin-events", twin.getTwinId(), event);
    }
}