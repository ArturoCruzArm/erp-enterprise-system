package com.erp.system.inventory.dto;

import com.erp.system.inventory.enums.MovementType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockMovementDto {
    
    private Long productId;
    private Long warehouseId;
    private MovementType movementType;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private String notes;
    private String referenceType;
    private Long referenceId;
    private String referenceNumber;
}