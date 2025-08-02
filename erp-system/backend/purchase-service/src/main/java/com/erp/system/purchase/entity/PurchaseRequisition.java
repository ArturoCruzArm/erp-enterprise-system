package com.erp.system.purchase.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.purchase.enums.Priority;
import com.erp.system.purchase.enums.RequisitionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "purchase_requisitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PurchaseRequisition extends BaseEntity {
    
    @Column(name = "requisition_number", unique = true, nullable = false)
    @NotBlank(message = "Requisition number is required")
    @Size(max = 50)
    private String requisitionNumber;
    
    @Column(name = "title", nullable = false)
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RequisitionStatus status = RequisitionStatus.DRAFT;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private Priority priority = Priority.MEDIUM;
    
    @Column(name = "requested_by_user_id", nullable = false)
    @NotNull(message = "Requested by user is required")
    private Long requestedByUserId;
    
    @Column(name = "department_id")
    private Long departmentId;
    
    @Column(name = "cost_center")
    @Size(max = 50)
    private String costCenter;
    
    @Column(name = "budget_code")
    @Size(max = 50)
    private String budgetCode;
    
    @Column(name = "requested_date", nullable = false)
    @NotNull(message = "Requested date is required")
    private LocalDate requestedDate;
    
    @Column(name = "required_date")
    private LocalDate requiredDate;
    
    @Column(name = "justification", columnDefinition = "TEXT")
    private String justification;
    
    @Column(name = "estimated_total", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Estimated total must be positive")
    private BigDecimal estimatedTotal;
    
    @Column(name = "actual_total", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Actual total must be positive")
    private BigDecimal actualTotal;
    
    @Column(name = "currency", length = 3)
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency = "USD";
    
    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;
    
    @Column(name = "approved_date")
    private LocalDateTime approvedDate;
    
    @Column(name = "approval_comments", columnDefinition = "TEXT")
    private String approvalComments;
    
    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;
    
    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;
    
    @Column(name = "project_code")
    @Size(max = 50)
    private String projectCode;
    
    @Column(name = "is_urgent")
    private Boolean isUrgent = false;
    
    @Column(name = "is_capital_expenditure")
    private Boolean isCapitalExpenditure = false;
    
    @Column(name = "attachment_urls", columnDefinition = "TEXT")
    private String attachmentUrls;
    
    @OneToMany(mappedBy = "purchaseRequisition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseRequisitionItem> items;
    
    @OneToMany(mappedBy = "purchaseRequisition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RequisitionApproval> approvals;
    
    @OneToMany(mappedBy = "purchaseRequisition", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseOrder> purchaseOrders;
    
    public void calculateEstimatedTotal() {
        if (items != null && !items.isEmpty()) {
            estimatedTotal = items.stream()
                    .map(item -> item.getEstimatedUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
    
    public boolean requiresApproval() {
        return estimatedTotal != null && estimatedTotal.compareTo(new BigDecimal("1000")) > 0;
    }
    
    public boolean isOverdue() {
        return requiredDate != null && 
               requiredDate.isBefore(LocalDate.now()) && 
               status != RequisitionStatus.COMPLETED && 
               status != RequisitionStatus.CANCELLED;
    }
}