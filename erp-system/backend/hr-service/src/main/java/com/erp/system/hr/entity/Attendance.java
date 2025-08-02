package com.erp.system.hr.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.hr.enums.AttendanceStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Attendance extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;
    
    @Column(name = "date", nullable = false)
    @NotNull(message = "Date is required")
    private LocalDate date;
    
    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;
    
    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;
    
    @Column(name = "break_start_time")
    private LocalDateTime breakStartTime;
    
    @Column(name = "break_end_time")
    private LocalDateTime breakEndTime;
    
    @Column(name = "total_hours_worked")
    private Double totalHoursWorked;
    
    @Column(name = "overtime_hours")
    private Double overtimeHours;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status = AttendanceStatus.PRESENT;
    
    @Column(name = "work_location")
    private String workLocation;
    
    @Column(name = "is_remote_work")
    private Boolean isRemoteWork = false;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "approved_by_manager")
    private Boolean approvedByManager = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private Employee approvedBy;
    
    @Column(name = "approval_date")
    private LocalDateTime approvalDate;
}