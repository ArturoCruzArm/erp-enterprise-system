package com.erp.system.inventory.controller;

import com.erp.system.inventory.dto.StockMovementDto;
import com.erp.system.inventory.entity.InventoryMovement;
import com.erp.system.inventory.entity.WarehouseStock;
import com.erp.system.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    @PostMapping("/movements")
    public ResponseEntity<Void> processStockMovement(@Valid @RequestBody StockMovementDto movementDto) {
        inventoryService.processStockMovement(movementDto);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stock/{productId}/warehouse/{warehouseId}")
    public ResponseEntity<BigDecimal> getAvailableStock(@PathVariable Long productId, @PathVariable Long warehouseId) {
        BigDecimal availableStock = inventoryService.getAvailableStock(productId, warehouseId);
        return ResponseEntity.ok(availableStock);
    }
    
    @GetMapping("/stock/{productId}/total")
    public ResponseEntity<BigDecimal> getTotalStockByProduct(@PathVariable Long productId) {
        BigDecimal totalStock = inventoryService.getTotalStockByProduct(productId);
        return ResponseEntity.ok(totalStock);
    }
    
    @GetMapping("/stock/low-stock")
    public ResponseEntity<List<WarehouseStock>> getLowStockItems() {
        List<WarehouseStock> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(lowStockItems);
    }
    
    @GetMapping("/stock/out-of-stock")
    public ResponseEntity<List<WarehouseStock>> getOutOfStockItems() {
        List<WarehouseStock> outOfStockItems = inventoryService.getOutOfStockItems();
        return ResponseEntity.ok(outOfStockItems);
    }
    
    @GetMapping("/movements/{productId}")
    public ResponseEntity<List<InventoryMovement>> getMovementHistory(@PathVariable Long productId) {
        List<InventoryMovement> movements = inventoryService.getMovementHistory(productId);
        return ResponseEntity.ok(movements);
    }
    
    @PostMapping("/reserve")
    public ResponseEntity<Void> reserveStock(@RequestParam Long productId, 
                                           @RequestParam Long warehouseId, 
                                           @RequestParam BigDecimal quantity) {
        inventoryService.reserveStock(productId, warehouseId, quantity);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/release-reservation")
    public ResponseEntity<Void> releaseReservation(@RequestParam Long productId, 
                                                  @RequestParam Long warehouseId, 
                                                  @RequestParam BigDecimal quantity) {
        inventoryService.releaseReservation(productId, warehouseId, quantity);
        return ResponseEntity.ok().build();
    }
}