package com.erp.system.inventory.entity;

import com.erp.system.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "warehouse_stock", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"warehouse_id", "product_id"}))
public class WarehouseStock extends BaseEntity {
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "quantity_on_hand", precision = 15, scale = 2, nullable = false)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "quantity_reserved", precision = 15, scale = 2, nullable = false)
    private BigDecimal quantityReserved = BigDecimal.ZERO;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "quantity_available", precision = 15, scale = 2, nullable = false)
    private BigDecimal quantityAvailable = BigDecimal.ZERO;
    
    @PositiveOrZero
    @Column(name = "minimum_stock", precision = 15, scale = 2)
    private BigDecimal minimumStock = BigDecimal.ZERO;
    
    @PositiveOrZero
    @Column(name = "maximum_stock", precision = 15, scale = 2)
    private BigDecimal maximumStock;
    
    @Column(name = "location")
    private String location;
    
    @PrePersist
    @PreUpdate
    private void calculateAvailableQuantity() {
        if (quantityOnHand != null && quantityReserved != null) {
            quantityAvailable = quantityOnHand.subtract(quantityReserved);
        }
    }
}