package com.erp.system.inventory.repository;

import com.erp.system.inventory.entity.WarehouseStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseStockRepository extends JpaRepository<WarehouseStock, Long> {
    
    Optional<WarehouseStock> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
    
    List<WarehouseStock> findByProductId(Long productId);
    
    List<WarehouseStock> findByWarehouseId(Long warehouseId);
    
    @Query("SELECT SUM(ws.quantityOnHand) FROM WarehouseStock ws WHERE ws.product.id = :productId")
    BigDecimal getTotalStockByProduct(@Param("productId") Long productId);
    
    @Query("SELECT SUM(ws.quantityAvailable) FROM WarehouseStock ws WHERE ws.product.id = :productId")
    BigDecimal getAvailableStockByProduct(@Param("productId") Long productId);
    
    @Query("SELECT ws FROM WarehouseStock ws WHERE ws.quantityOnHand <= ws.minimumStock")
    List<WarehouseStock> findLowStockItems();
    
    @Query("SELECT ws FROM WarehouseStock ws WHERE ws.quantityOnHand = 0")
    List<WarehouseStock> findOutOfStockItems();
    
    @Modifying
    @Query("UPDATE WarehouseStock ws SET ws.quantityOnHand = ws.quantityOnHand + :quantity, " +
           "ws.quantityAvailable = ws.quantityOnHand + :quantity - ws.quantityReserved " +
           "WHERE ws.warehouse.id = :warehouseId AND ws.product.id = :productId")
    void addStock(@Param("warehouseId") Long warehouseId, 
                  @Param("productId") Long productId, 
                  @Param("quantity") BigDecimal quantity);
    
    @Modifying
    @Query("UPDATE WarehouseStock ws SET ws.quantityOnHand = ws.quantityOnHand - :quantity, " +
           "ws.quantityAvailable = ws.quantityOnHand - :quantity - ws.quantityReserved " +
           "WHERE ws.warehouse.id = :warehouseId AND ws.product.id = :productId " +
           "AND ws.quantityOnHand >= :quantity")
    int removeStock(@Param("warehouseId") Long warehouseId, 
                    @Param("productId") Long productId, 
                    @Param("quantity") BigDecimal quantity);
}