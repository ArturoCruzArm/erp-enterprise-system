package com.erp.system.inventory.service;

import com.erp.system.inventory.dto.StockMovementDto;
import com.erp.system.inventory.entity.InventoryMovement;
import com.erp.system.inventory.entity.Product;
import com.erp.system.inventory.entity.Warehouse;
import com.erp.system.inventory.entity.WarehouseStock;
import com.erp.system.inventory.enums.MovementType;
import com.erp.system.inventory.repository.InventoryMovementRepository;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.WarehouseRepository;
import com.erp.system.inventory.repository.WarehouseStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    
    private final InventoryMovementRepository movementRepository;
    private final WarehouseStockRepository stockRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    
    @Transactional
    public void processStockMovement(StockMovementDto movementDto) {
        Product product = productRepository.findById(movementDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Warehouse warehouse = warehouseRepository.findById(movementDto.getWarehouseId())
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));
        
        WarehouseStock stock = stockRepository.findByWarehouseIdAndProductId(
                movementDto.getWarehouseId(), movementDto.getProductId())
                .orElseGet(() -> createNewStock(warehouse, product));
        
        BigDecimal quantityBefore = stock.getQuantityOnHand();
        
        // Process movement based on type
        switch (movementDto.getMovementType()) {
            case IN, PURCHASE, RETURN, PRODUCTION, ADJUSTMENT -> {
                if (movementDto.getMovementType() == MovementType.ADJUSTMENT && 
                    movementDto.getQuantity().compareTo(BigDecimal.ZERO) < 0) {
                    // Negative adjustment - reduce stock
                    BigDecimal absQuantity = movementDto.getQuantity().abs();
                    if (stock.getQuantityOnHand().compareTo(absQuantity) < 0) {
                        throw new RuntimeException("Insufficient stock for adjustment");
                    }
                    stock.setQuantityOnHand(stock.getQuantityOnHand().subtract(absQuantity));
                } else {
                    // Positive movement - add stock
                    stock.setQuantityOnHand(stock.getQuantityOnHand().add(movementDto.getQuantity()));
                }
            }
            case OUT, SALE, TRANSFER -> {
                // Negative movement - remove stock
                if (stock.getQuantityOnHand().compareTo(movementDto.getQuantity()) < 0) {
                    throw new RuntimeException("Insufficient stock available");
                }
                stock.setQuantityOnHand(stock.getQuantityOnHand().subtract(movementDto.getQuantity()));
            }
        }
        
        // Update available quantity
        stock.setQuantityAvailable(stock.getQuantityOnHand().subtract(stock.getQuantityReserved()));
        stockRepository.save(stock);
        
        // Create movement record
        InventoryMovement movement = new InventoryMovement();
        movement.setMovementNumber(generateMovementNumber());
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setMovementType(movementDto.getMovementType());
        movement.setQuantity(movementDto.getQuantity());
        movement.setUnitCost(movementDto.getUnitCost() != null ? movementDto.getUnitCost() : BigDecimal.ZERO);
        movement.setQuantityBefore(quantityBefore);
        movement.setQuantityAfter(stock.getQuantityOnHand());
        movement.setMovementDate(LocalDateTime.now());
        movement.setNotes(movementDto.getNotes());
        movement.setReferenceType(movementDto.getReferenceType());
        movement.setReferenceId(movementDto.getReferenceId());
        movement.setReferenceNumber(movementDto.getReferenceNumber());
        
        movementRepository.save(movement);
        
        log.info("Processed stock movement: {} {} for product {} in warehouse {}", 
                movementDto.getMovementType(), movementDto.getQuantity(), 
                product.getSku(), warehouse.getCode());
    }
    
    public BigDecimal getAvailableStock(Long productId, Long warehouseId) {
        return stockRepository.findByWarehouseIdAndProductId(warehouseId, productId)
                .map(WarehouseStock::getQuantityAvailable)
                .orElse(BigDecimal.ZERO);
    }
    
    public BigDecimal getTotalStockByProduct(Long productId) {
        BigDecimal total = stockRepository.getTotalStockByProduct(productId);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public List<WarehouseStock> getLowStockItems() {
        return stockRepository.findLowStockItems();
    }
    
    public List<WarehouseStock> getOutOfStockItems() {
        return stockRepository.findOutOfStockItems();
    }
    
    public List<InventoryMovement> getMovementHistory(Long productId) {
        return movementRepository.findByProductId(productId);
    }
    
    @Transactional
    public void reserveStock(Long productId, Long warehouseId, BigDecimal quantity) {
        WarehouseStock stock = stockRepository.findByWarehouseIdAndProductId(warehouseId, productId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
        
        if (stock.getQuantityAvailable().compareTo(quantity) < 0) {
            throw new RuntimeException("Insufficient available stock for reservation");
        }
        
        stock.setQuantityReserved(stock.getQuantityReserved().add(quantity));
        stock.setQuantityAvailable(stock.getQuantityOnHand().subtract(stock.getQuantityReserved()));
        stockRepository.save(stock);
        
        log.info("Reserved {} units of product {} in warehouse {}", 
                quantity, productId, warehouseId);
    }
    
    @Transactional
    public void releaseReservation(Long productId, Long warehouseId, BigDecimal quantity) {
        WarehouseStock stock = stockRepository.findByWarehouseIdAndProductId(warehouseId, productId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
        
        BigDecimal newReserved = stock.getQuantityReserved().subtract(quantity);
        if (newReserved.compareTo(BigDecimal.ZERO) < 0) {
            newReserved = BigDecimal.ZERO;
        }
        
        stock.setQuantityReserved(newReserved);
        stock.setQuantityAvailable(stock.getQuantityOnHand().subtract(stock.getQuantityReserved()));
        stockRepository.save(stock);
        
        log.info("Released reservation of {} units of product {} in warehouse {}", 
                quantity, productId, warehouseId);
    }
    
    private WarehouseStock createNewStock(Warehouse warehouse, Product product) {
        WarehouseStock stock = new WarehouseStock();
        stock.setWarehouse(warehouse);
        stock.setProduct(product);
        stock.setQuantityOnHand(BigDecimal.ZERO);
        stock.setQuantityReserved(BigDecimal.ZERO);
        stock.setQuantityAvailable(BigDecimal.ZERO);
        stock.setMinimumStock(product.getMinimumStock());
        stock.setMaximumStock(product.getMaximumStock());
        return stock;
    }
    
    private String generateMovementNumber() {
        String prefix = "MOV-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        long count = movementRepository.count() + 1;
        return prefix + String.format("%06d", count);
    }
}