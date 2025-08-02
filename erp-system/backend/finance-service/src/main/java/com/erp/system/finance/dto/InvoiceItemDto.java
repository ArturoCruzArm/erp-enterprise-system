package com.erp.system.finance.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class InvoiceItemDto {
    
    private Long id;
    private Long productId;
    private String itemDescription;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    private BigDecimal taxPercentage;
    private BigDecimal taxAmount;
    private BigDecimal lineTotal;
    private Integer lineOrder;
}