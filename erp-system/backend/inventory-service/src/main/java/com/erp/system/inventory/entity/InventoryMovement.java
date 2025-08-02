package com.erp.system.inventory.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.inventory.enums.MovementType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "inventory_movements")
public class InventoryMovement extends BaseEntity {
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "movement_number", unique = true, nullable = false)
    private String movementNumber;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;
    
    @NotNull
    @Positive
    @Column(name = "quantity", precision = 15, scale = 2, nullable = false)
    private BigDecimal quantity;
    
    @NotNull
    @Column(name = "unit_cost", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitCost = BigDecimal.ZERO;
    
    @NotNull
    @Column(name = "total_cost", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalCost = BigDecimal.ZERO;
    
    @Column(name = "quantity_before", precision = 15, scale = 2)
    private BigDecimal quantityBefore;
    
    @Column(name = "quantity_after", precision = 15, scale = 2)
    private BigDecimal quantityAfter;
    
    @NotNull
    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate = LocalDateTime.now();
    
    @Size(max = 1000)
    private String notes;
    
    @Size(max = 100)
    @Column(name = "reference_type")
    private String referenceType;
    
    @Column(name = "reference_id")
    private Long referenceId;
    
    @Size(max = 100)
    @Column(name = "reference_number")
    private String referenceNumber;
    
    @PrePersist
    private void calculateTotalCost() {
        if (quantity != null && unitCost != null) {
            totalCost = quantity.multiply(unitCost);
        }
    }
}