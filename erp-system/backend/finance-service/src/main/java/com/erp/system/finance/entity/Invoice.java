package com.erp.system.finance.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.finance.enums.InvoiceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "invoice_number", unique = true, nullable = false)
    private String invoiceNumber;
    
    @NotNull
    @Column(name = "invoice_date", nullable = false)
    private LocalDate invoiceDate;
    
    @NotNull
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvoiceStatus status = InvoiceStatus.DRAFT;
    
    @NotNull
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Size(max = 200)
    @Column(name = "customer_name")
    private String customerName;
    
    @Size(max = 500)
    @Column(name = "billing_address")
    private String billingAddress;
    
    @NotNull
    @Positive
    @Column(name = "subtotal", precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal;
    
    @NotNull
    @Column(name = "tax_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @NotNull
    @Column(name = "discount_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @NotNull
    @Positive
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    @NotNull
    @Column(name = "paid_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal paidAmount = BigDecimal.ZERO;
    
    @NotNull
    @Column(name = "balance_due", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceDue;
    
    @Size(max = 1000)
    private String notes;
    
    @Size(max = 3)
    @Column(name = "currency_code", nullable = false)
    private String currencyCode = "USD";
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InvoiceItem> items;
    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Payment> payments;
}