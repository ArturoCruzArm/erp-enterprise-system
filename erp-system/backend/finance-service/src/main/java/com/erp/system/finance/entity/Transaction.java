package com.erp.system.finance.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.finance.enums.TransactionType;
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
@Table(name = "transactions")
public class Transaction extends BaseEntity {
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "transaction_number", unique = true, nullable = false)
    private String transactionNumber;
    
    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    
    @Size(max = 1000)
    private String description;
    
    @Size(max = 500)
    private String reference;
    
    @NotNull
    @Positive
    @Column(name = "total_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalAmount;
    
    @Column(name = "exchange_rate", precision = 10, scale = 4)
    private BigDecimal exchangeRate = BigDecimal.ONE;
    
    @Size(max = 3)
    @Column(name = "currency_code", nullable = false)
    private String currencyCode = "USD";
    
    @Column(name = "customer_id")
    private Long customerId;
    
    @Column(name = "supplier_id")
    private Long supplierId;
    
    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JournalEntry> journalEntries;
}