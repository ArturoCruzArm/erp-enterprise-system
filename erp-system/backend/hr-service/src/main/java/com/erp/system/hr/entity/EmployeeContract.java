package com.erp.system.hr.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.hr.enums.ContractType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "employee_contracts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EmployeeContract extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    @NotNull(message = "Contract type is required")
    private ContractType contractType;
    
    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @Column(name = "end_date")
    private LocalDate endDate;
    
    @Column(name = "salary", precision = 12, scale = 2, nullable = false)
    @NotNull(message = "Salary is required")
    private BigDecimal salary;
    
    @Column(name = "working_hours_per_week")
    private Integer workingHoursPerWeek = 40;
    
    @Column(name = "probation_period_months")
    private Integer probationPeriodMonths;
    
    @Column(name = "notice_period_days")
    private Integer noticePeriodDays;
    
    @Column(name = "vacation_days_per_year")
    private Integer vacationDaysPerYear = 20;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "contract_document_url")
    private String contractDocumentUrl;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}