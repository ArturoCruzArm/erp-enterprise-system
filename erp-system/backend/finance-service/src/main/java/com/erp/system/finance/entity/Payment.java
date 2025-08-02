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
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "payment_number", unique = true, nullable = false)
    private String paymentNumber;
    
    @NotNull
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;
    
    @NotNull
    @Positive
    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;
    
    @Size(max = 100)
    @Column(name = "reference_number")
    private String referenceNumber;
    
    @Size(max = 500)
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id")
    private BankAccount bankAccount;
}