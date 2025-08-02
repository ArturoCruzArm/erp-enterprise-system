package com.erp.system.hr.dto;

import com.erp.system.hr.enums.EmployeeStatus;
import com.erp.system.hr.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {
    
    private Long id;
    
    @NotBlank(message = "Employee code is required")
    private String employeeCode;
    
    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;
    
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;
    
    private String nationalId;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private String city;
    private String country;
    private String postalCode;
    
    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;
    
    private LocalDate terminationDate;
    private EmployeeStatus status;
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
    private BigDecimal salary;
    
    private String emergencyContactName;
    
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid emergency contact phone")
    private String emergencyContactPhone;
    
    private String bankAccountNumber;
    private String taxId;
    private String profileImageUrl;
    private String notes;
    
    // Department and Position info
    private Long departmentId;
    private String departmentName;
    private Long positionId;
    private String positionTitle;
    
    // Manager info
    private Long managerId;
    private String managerName;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}