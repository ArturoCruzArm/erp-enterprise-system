package com.erp.system.production.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.production.enums.ProductionOrderStatus;
import com.erp.system.production.enums.Priority;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "production_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductionOrder extends BaseEntity {
    
    @Column(name = "order_number", unique = true, nullable = false)
    @NotBlank(message = "Order number is required")
    @Size(max = 50)
    private String orderNumber;
    
    @Column(name = "product_id", nullable = false)
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @Column(name = "product_code")
    @Size(max = 50)
    private String productCode;
    
    @Column(name = "product_name")
    @Size(max = 200)
    private String productName;
    
    @Column(name = "quantity_to_produce", nullable = false)
    @NotNull(message = "Quantity to produce is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantityToProduce;
    
    @Column(name = "quantity_produced")
    @Min(value = 0, message = "Quantity produced cannot be negative")
    private Integer quantityProduced = 0;
    
    @Column(name = "quantity_good")
    @Min(value = 0, message = "Quantity good cannot be negative")
    private Integer quantityGood = 0;
    
    @Column(name = "quantity_rejected")
    @Min(value = 0, message = "Quantity rejected cannot be negative")
    private Integer quantityRejected = 0;
    
    @Column(name = "unit_of_measure")
    @Size(max = 20)
    private String unitOfMeasure;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductionOrderStatus status = ProductionOrderStatus.PLANNED;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority = Priority.MEDIUM;
    
    @Column(name = "sales_order_id")
    private Long salesOrderId;
    
    @Column(name = "sales_order_number")
    @Size(max = 50)
    private String salesOrderNumber;
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "customer_name")
    @Size(max = 200)
    private String customerName;
    
    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;
    
    @Column(name = "planned_end_date")
    private LocalDate plannedEndDate;
    
    @Column(name = "actual_start_date")
    private LocalDateTime actualStartDate;
    
    @Column(name = "actual_end_date")
    private LocalDateTime actualEndDate;
    
    @Column(name = "due_date")
    private LocalDate dueDate;
    
    @Column(name = "estimated_duration_hours", precision = 8, scale = 2)
    private BigDecimal estimatedDurationHours;
    
    @Column(name = "actual_duration_hours", precision = 8, scale = 2)
    private BigDecimal actualDurationHours;
    
    @Column(name = "estimated_cost", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Estimated cost must be non-negative")
    private BigDecimal estimatedCost;
    
    @Column(name = "actual_cost", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Actual cost must be non-negative")
    private BigDecimal actualCost;
    
    @Column(name = "work_center_id")
    private Long workCenterId;
    
    @Column(name = "work_center_name")
    @Size(max = 100)
    private String workCenterName;
    
    @Column(name = "routing_id")
    private Long routingId;
    
    @Column(name = "bom_id")
    private Long bomId;
    
    @Column(name = "supervisor_user_id")
    private Long supervisorUserId;
    
    @Column(name = "created_by_user_id")
    private Long createdByUserId;
    
    @Column(name = "batch_number")
    @Size(max = 50)
    private String batchNumber;
    
    @Column(name = "lot_number")
    @Size(max = 50)
    private String lotNumber;
    
    @Column(name = "quality_checked")
    private Boolean qualityChecked = false;
    
    @Column(name = "quality_approved")
    private Boolean qualityApproved = false;
    
    @Column(name = "quality_check_date")
    private LocalDateTime qualityCheckDate;
    
    @Column(name = "quality_comments", columnDefinition = "TEXT")
    private String qualityComments;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "is_rush_order")
    private Boolean isRushOrder = false;
    
    @Column(name = "is_rework")
    private Boolean isRework = false;
    
    @Column(name = "original_order_id")
    private Long originalOrderId;
    
    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionOrderOperation> operations;
    
    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionOrderMaterial> materials;
    
    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionOrderStatusHistory> statusHistory;
    
    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QualityInspection> qualityInspections;
    
    @OneToMany(mappedBy = "productionOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductionTimeTracking> timeTrackings;
    
    public BigDecimal getCompletionPercentage() {
        if (quantityToProduce == null || quantityToProduce == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(quantityProduced)
                .divide(new BigDecimal(quantityToProduce), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    public BigDecimal getYieldPercentage() {
        if (quantityProduced == null || quantityProduced == 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(quantityGood)
                .divide(new BigDecimal(quantityProduced), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
    
    public boolean isOverdue() {
        return dueDate != null && 
               dueDate.isBefore(LocalDate.now()) && 
               status != ProductionOrderStatus.COMPLETED && 
               status != ProductionOrderStatus.CANCELLED;
    }
    
    public boolean isDelayed() {
        return plannedEndDate != null && 
               plannedEndDate.isBefore(LocalDate.now()) && 
               status != ProductionOrderStatus.COMPLETED && 
               status != ProductionOrderStatus.CANCELLED;
    }
    
    public Integer getRemainingQuantity() {
        return quantityToProduce - (quantityProduced != null ? quantityProduced : 0);
    }
}