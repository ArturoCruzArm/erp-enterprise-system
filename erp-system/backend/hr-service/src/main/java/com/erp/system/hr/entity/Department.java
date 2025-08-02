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
@Table(name = "departments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Department extends BaseEntity {
    
    @Column(name = "code", unique = true, nullable = false)
    @NotBlank(message = "Department code is required")
    @Size(max = 20)
    private String code;
    
    @Column(name = "name", nullable = false)
    @NotBlank(message = "Department name is required")
    @Size(max = 100)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;
    
    @Column(name = "location")
    @Size(max = 100)
    private String location;
    
    @Column(name = "cost_center")
    @Size(max = 50)
    private String costCenter;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_department_id")
    private Department parentDepartment;
    
    @OneToMany(mappedBy = "parentDepartment", cascade = CascadeType.ALL)
    private List<Department> subDepartments;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_employee_id")
    private Employee head;
    
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Employee> employees;
    
    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private List<Position> positions;
}