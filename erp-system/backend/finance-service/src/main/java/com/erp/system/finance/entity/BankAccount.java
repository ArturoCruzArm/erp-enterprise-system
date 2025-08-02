package com.erp.system.finance.entity;

import com.erp.system.entity.BaseEntity;
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
@Table(name = "bank_accounts")
public class BankAccount extends BaseEntity {
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "account_name", nullable = false)
    private String accountName;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "bank_name", nullable = false)
    private String bankName;
    
    @Size(max = 20)
    @Column(name = "routing_number")
    private String routingNumber;
    
    @Size(max = 20)
    @Column(name = "swift_code")
    private String swiftCode;
    
    @NotNull
    @Column(name = "current_balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal currentBalance = BigDecimal.ZERO;
    
    @Size(max = 3)
    @Column(name = "currency_code", nullable = false)
    private String currencyCode = "USD";
    
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;
    
    @OneToMany(mappedBy = "bankAccount", cascade = CascadeType.ALL)
    private List<Payment> payments;
}