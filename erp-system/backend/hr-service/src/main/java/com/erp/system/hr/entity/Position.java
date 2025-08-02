package com.erp.system.hr.entity;

import com.erp.system.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "positions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Position extends BaseEntity {
    
    @Column(name = "code", unique = true, nullable = false)
    @NotBlank(message = "Position code is required")
    @Size(max = 20)
    private String code;
    
    @Column(name = "title", nullable = false)
    @NotBlank(message = "Position title is required")
    @Size(max = 100)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;
    
    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;
    
    @Column(name = "min_salary", precision = 12, scale = 2)
    private BigDecimal minSalary;
    
    @Column(name = "max_salary", precision = 12, scale = 2)
    private BigDecimal maxSalary;
    
    @Column(name = "level", nullable = false)
    private Integer level = 1;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
    
    @OneToMany(mappedBy = "position", cascade = CascadeType.ALL)
    private List<Employee> employees;
}