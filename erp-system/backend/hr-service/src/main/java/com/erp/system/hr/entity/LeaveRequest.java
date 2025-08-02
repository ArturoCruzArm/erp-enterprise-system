package com.erp.system.hr.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.hr.enums.LeaveRequestStatus;
import com.erp.system.hr.enums.LeaveType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeaveRequest extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false)
    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;
    
    @Column(name = "start_date", nullable = false)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @Column(name = "end_date", nullable = false)
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    @Column(name = "days_requested", nullable = false)
    private Integer daysRequested;
    
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LeaveRequestStatus status = LeaveRequestStatus.PENDING;
    
    @Column(name = "submitted_date", nullable = false)
    private LocalDateTime submittedDate = LocalDateTime.now();
    
    @Column(name = "reviewed_date")
    private LocalDateTime reviewedDate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private Employee reviewedBy;
    
    @Column(name = "review_comments", columnDefinition = "TEXT")
    private String reviewComments;
    
    @Column(name = "is_half_day")
    private Boolean isHalfDay = false;
    
    @Column(name = "contact_during_leave")
    private String contactDuringLeave;
    
    @Column(name = "handover_notes", columnDefinition = "TEXT")
    private String handoverNotes;
}