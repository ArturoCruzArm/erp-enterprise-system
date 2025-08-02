package com.erp.system.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDto {
    
    private Long id;
    
    @NotBlank(message = "Department code is required")
    @Size(max = 20)
    private String code;
    
    @NotBlank(message = "Department name is required")
    @Size(max = 100)
    private String name;
    
    private String description;
    private BigDecimal budget;
    
    @Size(max = 100)
    private String location;
    
    @Size(max = 50)
    private String costCenter;
    
    private Boolean isActive;
    
    // Parent department
    private Long parentDepartmentId;
    private String parentDepartmentName;
    
    // Department head
    private Long headEmployeeId;
    private String headEmployeeName;
    
    // Statistics
    private Integer employeeCount;
    private List<DepartmentDto> subDepartments;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}