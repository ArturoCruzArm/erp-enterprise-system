package com.erp.system.inventory.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.inventory.enums.ProductStatus;
import com.erp.system.inventory.enums.UnitOfMeasure;
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
@Table(name = "products")
public class Product extends BaseEntity {
    
    @NotBlank
    @Size(max = 50)
    @Column(name = "sku", unique = true, nullable = false)
    private String sku;
    
    @NotBlank
    @Size(max = 200)
    @Column(name = "name", nullable = false)
    private String name;
    
    @Size(max = 1000)
    private String description;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure", nullable = false)
    private UnitOfMeasure unitOfMeasure;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "cost_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal costPrice;
    
    @NotNull
    @PositiveOrZero
    @Column(name = "selling_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal sellingPrice;
    
    @PositiveOrZero
    @Column(name = "minimum_stock", precision = 10, scale = 2)
    private BigDecimal minimumStock = BigDecimal.ZERO;
    
    @PositiveOrZero
    @Column(name = "maximum_stock", precision = 10, scale = 2)
    private BigDecimal maximumStock;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;
    
    @Size(max = 100)
    @Column(name = "brand")
    private String brand;
    
    @Size(max = 100)
    @Column(name = "model")
    private String model;
    
    @Size(max = 50)
    @Column(name = "barcode")
    private String barcode;
    
    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;
    
    @Size(max = 50)
    @Column(name = "weight_unit")
    private String weightUnit;
    
    @Column(name = "length", precision = 10, scale = 2)
    private BigDecimal length;
    
    @Column(name = "width", precision = 10, scale = 2)
    private BigDecimal width;
    
    @Column(name = "height", precision = 10, scale = 2)
    private BigDecimal height;
    
    @Size(max = 10)
    @Column(name = "dimension_unit")
    private String dimensionUnit = "cm";
    
    @Column(name = "track_inventory", nullable = false)
    private Boolean trackInventory = true;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryMovement> inventoryMovements;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Warehouse> warehouses;
}