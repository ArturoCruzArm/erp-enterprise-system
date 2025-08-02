package com.erp.system.inventory.repository;

import com.erp.system.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    Optional<Warehouse> findByCode(String code);
    
    @Query("SELECT w FROM Warehouse w WHERE w.isMain = true AND w.active = true")
    Optional<Warehouse> findMainWarehouse();
    
    @Query("SELECT w FROM Warehouse w WHERE w.active = true ORDER BY w.name")
    List<Warehouse> findActiveWarehouses();
    
    boolean existsByCode(String code);
}