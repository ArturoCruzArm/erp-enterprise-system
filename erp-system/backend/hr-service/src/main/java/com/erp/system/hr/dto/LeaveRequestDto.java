package com.erp.system.hr.dto;

import com.erp.system.hr.enums.LeaveRequestStatus;
import com.erp.system.hr.enums.LeaveType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDto {
    
    private Long id;
    
    @NotNull(message = "Employee ID is required")
    private Long employeeId;
    private String employeeName;
    
    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    private Integer daysRequested;
    private String reason;
    private LeaveRequestStatus status;
    private LocalDateTime submittedDate;
    private LocalDateTime reviewedDate;
    
    // Reviewer info
    private Long reviewedById;
    private String reviewedByName;
    private String reviewComments;
    
    private Boolean isHalfDay;
    private String contactDuringLeave;
    private String handoverNotes;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}