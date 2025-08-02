package com.erp.system.finance.entity;

import com.erp.system.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "journal_entries")
public class JournalEntry extends BaseEntity {
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private ChartOfAccounts account;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "debit_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal debitAmount = BigDecimal.ZERO;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "credit_amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal creditAmount = BigDecimal.ZERO;
    
    @Size(max = 500)
    private String description;
    
    @Column(name = "entry_order")
    private Integer entryOrder;
}