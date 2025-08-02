package com.erp.system.inventory.dto;

import com.erp.system.inventory.enums.ProductStatus;
import com.erp.system.inventory.enums.UnitOfMeasure;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    
    private Long id;
    private String sku;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private UnitOfMeasure unitOfMeasure;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private BigDecimal minimumStock;
    private BigDecimal maximumStock;
    private ProductStatus status;
    private String brand;
    private String model;
    private String barcode;
    private BigDecimal weight;
    private String weightUnit;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private String dimensionUnit;
    private Boolean trackInventory;
    private BigDecimal totalStock;
    private BigDecimal availableStock;
}