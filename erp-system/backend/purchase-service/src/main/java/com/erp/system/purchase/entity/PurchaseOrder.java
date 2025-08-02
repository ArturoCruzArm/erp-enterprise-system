package com.erp.system.purchase.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.purchase.enums.PurchaseOrderStatus;
import com.erp.system.purchase.enums.Priority;
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
@Table(name = "purchase_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseOrder extends BaseEntity {
    
    @Column(name = "po_number", unique = true, nullable = false)
    @NotBlank(message = "PO number is required")
    @Size(max = 50)
    private String poNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    @NotNull(message = "Supplier is required")
    private Supplier supplier;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_requisition_id")
    private PurchaseRequisition purchaseRequisition;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority = Priority.MEDIUM;
    
    @Column(name = "order_date", nullable = false)
    @NotNull(message = "Order date is required")
    private LocalDate orderDate;
    
    @Column(name = "expected_delivery_date")
    private LocalDate expectedDeliveryDate;
    
    @Column(name = "requested_delivery_date")
    private LocalDate requestedDeliveryDate;
    
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;
    
    @Column(name = "billing_address", columnDefinition = "TEXT")
    private String billingAddress;
    
    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    @NotNull(message = "Subtotal is required")
    @DecimalMin(value = "0.0", message = "Subtotal must be positive")
    private BigDecimal subtotal;
    
    @Column(name = "tax_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Tax amount must be non-negative")
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "shipping_cost", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Shipping cost must be non-negative")
    private BigDecimal shippingCost = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Discount amount must be non-negative")
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    @NotNull(message = "Total amount is required")
    @DecimalMin(value = "0.0", message = "Total amount must be positive")
    private BigDecimal totalAmount;
    
    @Column(name = "currency", length = 3)
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency = "USD";
    
    @Column(name = "payment_terms_days")
    @Min(value = 0, message = "Payment terms must be non-negative")
    private Integer paymentTermsDays;
    
    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_by_user_id")
    private Long createdByUserId;
    
    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;
    
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;
    
    @Column(name = "sent_to_supplier_date")
    private LocalDateTime sentToSupplierDate;
    
    @Column(name = "acknowledgment_date")
    private LocalDateTime acknowledgmentDate;
    
    @Column(name = "is_urgent")
    private Boolean isUrgent = false;
    
    @Column(name = "is_drop_shipment")
    private Boolean isDropShipment = false;
    
    @Column(name = "tracking_number")
    @Size(max = 100)
    private String trackingNumber;
    
    @Column(name = "supplier_reference")
    @Size(max = 100)
    private String supplierReference;
    
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseOrderItem> items;
    
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GoodsReceipt> goodsReceipts;
    
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseOrderStatusHistory> statusHistory;
    
    public void calculateTotals() {
        if (items != null && !items.isEmpty()) {
            subtotal = items.stream()
                    .map(PurchaseOrderItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            totalAmount = subtotal
                    .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                    .add(shippingCost != null ? shippingCost : BigDecimal.ZERO)
                    .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
        }
    }
    
    public BigDecimal getRemainingAmount() {
        if (goodsReceipts == null || goodsReceipts.isEmpty()) {
            return totalAmount;
        }
        
        BigDecimal receivedAmount = goodsReceipts.stream()
                .map(GoodsReceipt::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return totalAmount.subtract(receivedAmount);
    }
    
    public boolean isOverdue() {
        return expectedDeliveryDate != null && 
               expectedDeliveryDate.isBefore(LocalDate.now()) && 
               status != PurchaseOrderStatus.COMPLETED && 
               status != PurchaseOrderStatus.CANCELLED;
    }
}