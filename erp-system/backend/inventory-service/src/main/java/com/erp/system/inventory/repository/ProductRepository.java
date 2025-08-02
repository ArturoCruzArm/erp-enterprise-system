package com.erp.system.inventory.repository;

import com.erp.system.inventory.entity.Product;
import com.erp.system.inventory.enums.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Optional<Product> findBySku(String sku);
    
    Optional<Product> findByBarcode(String barcode);
    
    List<Product> findByStatus(ProductStatus status);
    
    List<Product> findByCategoryId(Long categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:name% AND p.active = true")
    List<Product> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT p FROM Product p WHERE p.brand = :brand AND p.active = true")
    List<Product> findByBrand(@Param("brand") String brand);
    
    @Query("SELECT p FROM Product p WHERE " +
           "(p.name LIKE %:keyword% OR p.description LIKE %:keyword% OR p.sku LIKE %:keyword%) " +
           "AND p.active = true")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p JOIN WarehouseStock ws ON p.id = ws.product.id " +
           "WHERE ws.quantityOnHand <= p.minimumStock AND p.active = true")
    List<Product> findLowStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.status = 'ACTIVE' AND p.active = true")
    Page<Product> findActiveProducts(Pageable pageable);
    
    boolean existsBySku(String sku);
    
    boolean existsByBarcode(String barcode);
}