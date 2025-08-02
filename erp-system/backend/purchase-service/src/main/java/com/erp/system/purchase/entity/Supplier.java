package com.erp.system.purchase.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.purchase.enums.SupplierStatus;
import com.erp.system.purchase.enums.SupplierType;
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
@Table(name = "suppliers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Supplier extends BaseEntity {
    
    @Column(name = "supplier_code", unique = true, nullable = false)
    @NotBlank(message = "Supplier code is required")
    @Size(max = 20)
    private String supplierCode;
    
    @Column(name = "company_name", nullable = false)
    @NotBlank(message = "Company name is required")
    @Size(max = 200)
    private String companyName;
    
    @Column(name = "legal_name")
    @Size(max = 200)
    private String legalName;
    
    @Column(name = "tax_id", unique = true)
    @Size(max = 50)
    private String taxId;
    
    @Column(name = "registration_number")
    @Size(max = 50)
    private String registrationNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "supplier_type", nullable = false)
    private SupplierType supplierType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SupplierStatus status = SupplierStatus.ACTIVE;
    
    @Column(name = "website")
    @Size(max = 100)
    private String website;
    
    @Column(name = "industry")
    @Size(max = 100)
    private String industry;
    
    @Column(name = "established_date")
    private LocalDate establishedDate;
    
    @Column(name = "employee_count")
    private Integer employeeCount;
    
    @Column(name = "annual_revenue", precision = 15, scale = 2)
    private BigDecimal annualRevenue;
    
    @Column(name = "credit_limit", precision = 12, scale = 2)
    @DecimalMin(value = "0.0", message = "Credit limit must be positive")
    private BigDecimal creditLimit;
    
    @Column(name = "payment_terms_days")
    @Min(value = 0, message = "Payment terms must be non-negative")
    private Integer paymentTermsDays = 30;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @DecimalMin(value = "0.0", message = "Discount must be non-negative")
    @DecimalMax(value = "100.0", message = "Discount cannot exceed 100%")
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @Column(name = "quality_rating", precision = 3, scale = 2)
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private BigDecimal qualityRating;
    
    @Column(name = "delivery_rating", precision = 3, scale = 2)
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private BigDecimal deliveryRating;
    
    @Column(name = "service_rating", precision = 3, scale = 2)
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "5.0")
    private BigDecimal serviceRating;
    
    @Column(name = "is_preferred_supplier")
    private Boolean isPreferredSupplier = false;
    
    @Column(name = "is_certified")
    private Boolean isCertified = false;
    
    @Column(name = "certification_details", columnDefinition = "TEXT")
    private String certificationDetails;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SupplierContact> contacts;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SupplierAddress> addresses;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SupplierProduct> supplierProducts;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PurchaseOrder> purchaseOrders;
    
    @OneToMany(mappedBy = "supplier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RequestForQuotation> rfqs;
    
    public BigDecimal getOverallRating() {
        if (qualityRating == null || deliveryRating == null || serviceRating == null) {
            return BigDecimal.ZERO;
        }
        return qualityRating.add(deliveryRating).add(serviceRating).divide(new BigDecimal("3"), 2, BigDecimal.ROUND_HALF_UP);
    }
}