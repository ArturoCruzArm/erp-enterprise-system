package com.erp.system.finance.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.finance.enums.AccountType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chart_of_accounts")
public class ChartOfAccounts extends BaseEntity {
    
    @NotBlank
    @Size(max = 20)
    @Column(name = "account_code", unique = true, nullable = false)
    private String accountCode;
    
    @NotBlank
    @Size(max = 200)
    @Column(name = "account_name", nullable = false)
    private String accountName;
    
    @Size(max = 500)
    private String description;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_account_id")
    private ChartOfAccounts parentAccount;
    
    @OneToMany(mappedBy = "parentAccount", cascade = CascadeType.ALL)
    private List<ChartOfAccounts> subAccounts;
    
    @Column(name = "current_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;
    
    @Column(name = "is_header", nullable = false)
    private Boolean isHeader = false;
    
    @Column(name = "level", nullable = false)
    private Integer level = 1;
    
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<JournalEntry> journalEntries;
}