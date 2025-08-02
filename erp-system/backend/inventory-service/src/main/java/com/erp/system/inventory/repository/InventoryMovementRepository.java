package com.erp.system.inventory.repository;

import com.erp.system.inventory.entity.InventoryMovement;
import com.erp.system.inventory.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    
    List<InventoryMovement> findByProductId(Long productId);
    
    List<InventoryMovement> findByWarehouseId(Long warehouseId);
    
    List<InventoryMovement> findByMovementType(MovementType movementType);
    
    @Query("SELECT im FROM InventoryMovement im WHERE im.product.id = :productId " +
           "AND im.warehouse.id = :warehouseId ORDER BY im.movementDate DESC")
    List<InventoryMovement> findByProductAndWarehouse(@Param("productId") Long productId, 
                                                      @Param("warehouseId") Long warehouseId);
    
    @Query("SELECT im FROM InventoryMovement im WHERE im.movementDate BETWEEN :startDate AND :endDate " +
           "ORDER BY im.movementDate DESC")
    List<InventoryMovement> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT im FROM InventoryMovement im WHERE im.referenceType = :referenceType " +
           "AND im.referenceId = :referenceId")
    List<InventoryMovement> findByReference(@Param("referenceType") String referenceType, 
                                           @Param("referenceId") Long referenceId);
    
    Page<InventoryMovement> findByActiveTrue(Pageable pageable);
    
    @Query("SELECT COUNT(im) FROM InventoryMovement im WHERE im.movementType = :movementType " +
           "AND im.movementDate BETWEEN :startDate AND :endDate")
    Long countByTypeAndDateRange(@Param("movementType") MovementType movementType,
                                @Param("startDate") LocalDateTime startDate,
                                @Param("endDate") LocalDateTime endDate);
}