package com.erp.system.quantum.entity;

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
 * Quantum Job Entity
 * Represents a quantum computing job submitted for execution
 */
@Entity
@Table(name = "quantum_jobs", indexes = {
    @Index(name = "idx_job_type_status", columnList = "job_type, status"),
    @Index(name = "idx_submitted_at", columnList = "submitted_at"),
    @Index(name = "idx_priority_status", columnList = "priority, status")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class QuantumJob extends BaseEntity {

    @NotBlank(message = "Job ID is required")
    @Column(name = "job_id", unique = true, nullable = false, length = 100)
    private String jobId;

    @NotBlank(message = "Job name is required")
    @Column(name = "job_name", nullable = false, length = 200)
    private String jobName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Job type is required")
    @Column(name = "job_type", nullable = false, length = 50)
    private JobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private JobStatus status = JobStatus.SUBMITTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private JobPriority priority = JobPriority.MEDIUM;

    @Column(name = "submitted_by", length = 100)
    private String submittedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "execution_time_ms")
    private Long executionTimeMs;

    // Algorithm Configuration
    @Enumerated(EnumType.STRING)
    @Column(name = "algorithm", nullable = false, length = 50)
    private QuantumAlgorithm algorithm;

    @Column(name = "circuit_depth")
    private Integer circuitDepth;

    @Column(name = "qubit_count")
    private Integer qubitCount;

    @Column(name = "shots")
    private Integer shots = 1024;

    @Column(name = "optimization_level")
    private Integer optimizationLevel = 1;

    // Problem Definition
    @Column(name = "problem_definition", columnDefinition = "TEXT")
    private String problemDefinition; // JSON representation of the problem

    @Column(name = "objective_function", columnDefinition = "TEXT")
    private String objectiveFunction;

    @Column(name = "constraints", columnDefinition = "TEXT")
    private String constraints; // JSON array of constraints

    @ElementCollection
    @CollectionTable(name = "job_parameters", 
                    joinColumns = @JoinColumn(name = "job_id"))
    @MapKeyColumn(name = "parameter_name")
    @Column(name = "parameter_value")
    private Map<String, String> parameters = new HashMap<>();

    // Quantum Backend Configuration
    @Column(name = "backend_type", length = 50)
    private String backendType = "SIMULATOR"; // SIMULATOR, IBM_QUANTUM, GOOGLE_QUANTUM, etc.

    @Column(name = "backend_name", length = 100)
    private String backendName;

    @Column(name = "quantum_provider", length = 50)
    private String quantumProvider;

    @Column(name = "coupling_map", columnDefinition = "TEXT")
    private String couplingMap; // JSON representation of qubit connectivity

    @Column(name = "noise_model", columnDefinition = "TEXT")
    private String noiseModel; // JSON representation of noise characteristics

    // Results and Output
    @Column(name = "result_data", columnDefinition = "TEXT")
    private String resultData; // JSON representation of results

    @Column(name = "optimal_value", precision = 20, scale = 10)
    private BigDecimal optimalValue;

    @Column(name = "convergence_achieved", nullable = false)
    private Boolean convergenceAchieved = false;

    @Column(name = "iteration_count")
    private Integer iterationCount;

    @Column(name = "final_energy", precision = 20, scale = 10)
    private BigDecimal finalEnergy;

    @Column(name = "success_probability", precision = 5, scale = 4)
    private BigDecimal successProbability;

    // Quality Metrics
    @Column(name = "fidelity", precision = 5, scale = 4)
    private BigDecimal fidelity;

    @Column(name = "error_rate", precision = 5, scale = 4)
    private BigDecimal errorRate;

    @Column(name = "coherence_time_us", precision = 10, scale = 3)
    private BigDecimal coherenceTimeUs;

    @Column(name = "gate_error_rate", precision = 8, scale = 6)
    private BigDecimal gateErrorRate;

    // Cost and Resource Usage
    @Column(name = "quantum_cost", precision = 10, scale = 4)
    private BigDecimal quantumCost;

    @Column(name = "classical_preprocessing_time_ms")
    private Long classicalPreprocessingTimeMs;

    @Column(name = "quantum_execution_time_ms")
    private Long quantumExecutionTimeMs;

    @Column(name = "classical_postprocessing_time_ms")
    private Long classicalPostprocessingTimeMs;

    @Column(name = "total_shots_used")
    private Long totalShotsUsed;

    // Error and Debugging
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    private Integer maxRetries = 3;

    // Relationships
    @OneToMany(mappedBy = "quantumJob", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuantumCircuit> circuits = new ArrayList<>();

    @OneToMany(mappedBy = "quantumJob", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OptimizationResult> optimizationResults = new ArrayList<>();

    // Tags and Metadata
    @ElementCollection
    @CollectionTable(name = "job_tags", 
                    joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "job_metadata", 
                    joinColumns = @JoinColumn(name = "job_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();

    /**
     * Types of quantum computing jobs
     */
    public enum JobType {
        OPTIMIZATION,           // Combinatorial optimization problems
        SIMULATION,            // Quantum system simulation
        MACHINE_LEARNING,      // Quantum machine learning
        CRYPTOGRAPHY,          // Quantum cryptographic operations
        CHEMISTRY,             // Molecular simulation
        FINANCE,               // Financial modeling and risk analysis
        SUPPLY_CHAIN,          // Supply chain optimization
        SCHEDULING,            // Resource scheduling problems
        ROUTING,               // Vehicle routing and logistics
        PORTFOLIO,             // Portfolio optimization
        FACTORIZATION,         // Integer factorization
        SEARCH,                // Quantum search algorithms
        SAMPLING               // Quantum sampling problems
    }

    /**
     * Quantum algorithms available
     */
    public enum QuantumAlgorithm {
        QAOA,                  // Quantum Approximate Optimization Algorithm
        VQE,                   // Variational Quantum Eigensolver
        GROVER,                // Grover's Search Algorithm
        SHOR,                  // Shor's Factorization Algorithm
        QUANTUM_ANNEALING,     // Quantum Annealing
        QSVM,                  // Quantum Support Vector Machine
        QNN,                   // Quantum Neural Network
        QGAN,                  // Quantum Generative Adversarial Network
        HHL,                   // Harrow-Hassidim-Lloyd Algorithm
        QUANTUM_WALK,          // Quantum Walk
        AMPLITUDE_ESTIMATION,  // Quantum Amplitude Estimation
        PHASE_ESTIMATION,      // Quantum Phase Estimation
        VARIATIONAL_CLASSIFIER, // Variational Quantum Classifier
        QPCA,                  // Quantum Principal Component Analysis
        QUANTUM_FOURIER_TRANSFORM // Quantum Fourier Transform
    }

    /**
     * Job execution status
     */
    public enum JobStatus {
        SUBMITTED,
        QUEUED,
        PREPROCESSING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED,
        TIMEOUT,
        RETRYING
    }

    /**
     * Job execution priority
     */
    public enum JobPriority {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    // Business methods
    public boolean isCompleted() {
        return status == JobStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == JobStatus.FAILED || status == JobStatus.TIMEOUT;
    }

    public boolean isRunning() {
        return status == JobStatus.RUNNING || status == JobStatus.PREPROCESSING;
    }

    public boolean canRetry() {
        return retryCount < maxRetries && isFailed();
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public void markAsStarted() {
        this.status = JobStatus.RUNNING;
        this.startedAt = LocalDateTime.now();
    }

    public void markAsCompleted() {
        this.status = JobStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
        
        if (startedAt != null) {
            this.executionTimeMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    public void markAsFailed(String errorMessage) {
        this.status = JobStatus.FAILED;
        this.completedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
        
        if (startedAt != null) {
            this.executionTimeMs = java.time.Duration.between(startedAt, completedAt).toMillis();
        }
    }

    public void addParameter(String name, String value) {
        this.parameters.put(name, value);
    }

    public String getParameter(String name) {
        return this.parameters.get(name);
    }

    public void addMetadata(String key, String value) {
        this.metadata.put(key, value);
    }

    public String getMetadata(String key) {
        return this.metadata.get(key);
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

    public BigDecimal calculateSpeedup() {
        if (classicalPreprocessingTimeMs == null || quantumExecutionTimeMs == null) {
            return BigDecimal.ZERO;
        }
        
        long totalClassicalTime = classicalPreprocessingTimeMs + 
                                 (classicalPostprocessingTimeMs != null ? classicalPostprocessingTimeMs : 0);
        
        if (quantumExecutionTimeMs == 0) {
            return BigDecimal.ZERO;
        }
        
        return new BigDecimal(totalClassicalTime).divide(new BigDecimal(quantumExecutionTimeMs), 4, java.math.RoundingMode.HALF_UP);
    }

    public BigDecimal calculateQuantumAdvantage() {
        // Simplified metric for quantum advantage
        if (successProbability == null || fidelity == null) {
            return BigDecimal.ZERO;
        }
        
        return successProbability.multiply(fidelity);
    }

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (jobId == null) {
            jobId = generateJobId();
        }
    }

    private String generateJobId() {
        return "qjob_" + System.currentTimeMillis() + "_" + 
               jobType.name().toLowerCase() + "_" + 
               Math.abs(hashCode() % 10000);
    }
}