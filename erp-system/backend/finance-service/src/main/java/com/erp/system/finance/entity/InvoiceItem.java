package com.erp.system.finance.entity;

import com.erp.system.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "invoice_items")
public class InvoiceItem extends BaseEntity {
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
    
    @Column(name = "product_id")
    private Long productId;
    
    @NotBlank
    @Size(max = 200)
    @Column(name = "item_description", nullable = false)
    private String itemDescription;
    
    @NotNull
    @Positive
    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;
    
    @NotNull
    @Positive
    @Column(name = "unit_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal unitPrice;
    
    @NotNull
    @Column(name = "discount_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @NotNull
    @Column(name = "discount_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @NotNull
    @Column(name = "tax_percentage", precision = 5, scale = 2, nullable = false)
    private BigDecimal taxPercentage = BigDecimal.ZERO;
    
    @NotNull
    @Column(name = "tax_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @NotNull
    @Positive
    @Column(name = "line_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal lineTotal;
    
    @Column(name = "line_order")
    private Integer lineOrder;
}