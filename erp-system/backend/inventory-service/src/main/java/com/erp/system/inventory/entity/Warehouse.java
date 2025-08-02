package com.erp.system.inventory.entity;

import com.erp.system.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "warehouses")
public class Warehouse extends BaseEntity {
    
    @NotBlank
    @Size(max = 100)
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "code", unique = true, nullable = false)
    private String code;
    
    @Size(max = 500)
    private String description;
    
    @Size(max = 500)
    private String address;
    
    @Size(max = 100)
    private String city;
    
    @Size(max = 100)
    private String state;
    
    @Size(max = 20)
    @Column(name = "postal_code")
    private String postalCode;
    
    @Size(max = 100)
    private String country;
    
    @Size(max = 20)
    @Column(name = "phone_number")
    private String phoneNumber;
    
    @Size(max = 100)
    private String email;
    
    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;
    
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WarehouseStock> stocks;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}