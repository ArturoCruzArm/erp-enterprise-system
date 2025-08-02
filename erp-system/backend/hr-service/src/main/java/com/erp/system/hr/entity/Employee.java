package com.erp.system.hr.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.hr.enums.EmployeeStatus;
import com.erp.system.hr.enums.Gender;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Employee extends BaseEntity {
    
    @Column(name = "employee_code", unique = true, nullable = false)
    @NotBlank(message = "Employee code is required")
    private String employeeCode;
    
    @Column(name = "first_name", nullable = false)
    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;
    
    @Column(name = "email", unique = true, nullable = false)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Column(name = "phone")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number")
    private String phone;
    
    @Column(name = "national_id", unique = true)
    private String nationalId;
    
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;
    
    @Column(name = "address")
    @Size(max = 255)
    private String address;
    
    @Column(name = "city")
    @Size(max = 50)
    private String city;
    
    @Column(name = "country")
    @Size(max = 50)
    private String country;
    
    @Column(name = "postal_code")
    @Size(max = 20)
    private String postalCode;
    
    @Column(name = "hire_date", nullable = false)
    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;
    
    @Column(name = "termination_date")
    private LocalDate terminationDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    
    @Column(name = "salary", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be positive")
    private BigDecimal salary;
    
    @Column(name = "emergency_contact_name")
    @Size(max = 100)
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_phone")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid emergency contact phone")
    private String emergencyContactPhone;
    
    @Column(name = "bank_account_number")
    private String bankAccountNumber;
    
    @Column(name = "tax_id")
    private String taxId;
    
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private Employee manager;
    
    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    private List<Employee> subordinates;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<EmployeeContract> contracts;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<LeaveRequest> leaveRequests;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Performance> performances;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL)
    private List<Attendance> attendances;
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}