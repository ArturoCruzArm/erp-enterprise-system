package com.erp.system.inventory.service;

import com.erp.system.inventory.dto.ProductDto;
import com.erp.system.inventory.entity.Category;
import com.erp.system.inventory.entity.Product;
import com.erp.system.inventory.entity.WarehouseStock;
import com.erp.system.inventory.repository.CategoryRepository;
import com.erp.system.inventory.repository.ProductRepository;
import com.erp.system.inventory.repository.WarehouseStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final WarehouseStockRepository warehouseStockRepository;
    
    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        if (productRepository.existsBySku(productDto.getSku())) {
            throw new RuntimeException("Product with SKU " + productDto.getSku() + " already exists");
        }
        
        Product product = mapToEntity(productDto);
        if (product.getSku() == null || product.getSku().isEmpty()) {
            product.setSku(generateSku());
        }
        
        Product savedProduct = productRepository.save(product);
        log.info("Created product: {} - {}", savedProduct.getSku(), savedProduct.getName());
        
        return mapToDto(savedProduct);
    }
    
    @Transactional
    public ProductDto updateProduct(Long id, ProductDto productDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Check if SKU is being changed and if it's already taken
        if (!product.getSku().equals(productDto.getSku()) && 
            productRepository.existsBySku(productDto.getSku())) {
            throw new RuntimeException("Product with SKU " + productDto.getSku() + " already exists");
        }
        
        updateProductFromDto(product, productDto);
        Product savedProduct = productRepository.save(product);
        log.info("Updated product: {} - {}", savedProduct.getSku(), savedProduct.getName());
        
        return mapToDto(savedProduct);
    }
    
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToDto(product);
    }
    
    public ProductDto getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found with SKU: " + sku));
        return mapToDto(product);
    }
    
    public Page<ProductDto> getAllProducts(Pageable pageable) {
        return productRepository.findActiveProducts(pageable)
                .map(this::mapToDto);
    }
    
    public Page<ProductDto> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable)
                .map(this::mapToDto);
    }
    
    public List<ProductDto> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public List<ProductDto> getLowStockProducts() {
        return productRepository.findLowStockProducts()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setActive(false);
        productRepository.save(product);
        log.info("Deleted product: {} - {}", product.getSku(), product.getName());
    }
    
    private String generateSku() {
        long count = productRepository.count() + 1;
        return "PRD-" + String.format("%06d", count);
    }
    
    private Product mapToEntity(ProductDto dto) {
        Product product = new Product();
        updateProductFromDto(product, dto);
        return product;
    }
    
    private void updateProductFromDto(Product product, ProductDto dto) {
        product.setSku(dto.getSku());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setUnitOfMeasure(dto.getUnitOfMeasure());
        product.setCostPrice(dto.getCostPrice());
        product.setSellingPrice(dto.getSellingPrice());
        product.setMinimumStock(dto.getMinimumStock());
        product.setMaximumStock(dto.getMaximumStock());
        product.setStatus(dto.getStatus());
        product.setBrand(dto.getBrand());
        product.setModel(dto.getModel());
        product.setBarcode(dto.getBarcode());
        product.setWeight(dto.getWeight());
        product.setWeightUnit(dto.getWeightUnit());
        product.setLength(dto.getLength());
        product.setWidth(dto.getWidth());
        product.setHeight(dto.getHeight());
        product.setDimensionUnit(dto.getDimensionUnit());
        product.setTrackInventory(dto.getTrackInventory());
        
        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }
    }
    
    private ProductDto mapToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setUnitOfMeasure(product.getUnitOfMeasure());
        dto.setCostPrice(product.getCostPrice());
        dto.setSellingPrice(product.getSellingPrice());
        dto.setMinimumStock(product.getMinimumStock());
        dto.setMaximumStock(product.getMaximumStock());
        dto.setStatus(product.getStatus());
        dto.setBrand(product.getBrand());
        dto.setModel(product.getModel());
        dto.setBarcode(product.getBarcode());
        dto.setWeight(product.getWeight());
        dto.setWeightUnit(product.getWeightUnit());
        dto.setLength(product.getLength());
        dto.setWidth(product.getWidth());
        dto.setHeight(product.getHeight());
        dto.setDimensionUnit(product.getDimensionUnit());
        dto.setTrackInventory(product.getTrackInventory());
        
        // Get stock information
        BigDecimal totalStock = warehouseStockRepository.getTotalStockByProduct(product.getId());
        BigDecimal availableStock = warehouseStockRepository.getAvailableStockByProduct(product.getId());
        dto.setTotalStock(totalStock != null ? totalStock : BigDecimal.ZERO);
        dto.setAvailableStock(availableStock != null ? availableStock : BigDecimal.ZERO);
        
        return dto;
    }
}