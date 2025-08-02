package com.erp.system.purchase.service;

import com.erp.system.purchase.dto.PurchaseOrderDto;
import com.erp.system.purchase.entity.PurchaseOrder;
import com.erp.system.purchase.enums.PurchaseOrderStatus;
import com.erp.system.purchase.mapper.PurchaseOrderMapper;
import com.erp.system.purchase.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PurchaseOrderService {
    
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final SupplierService supplierService;
    private final ApprovalWorkflowService approvalWorkflowService;
    private final EmailNotificationService emailNotificationService;
    private final PurchaseOrderEventService eventService;
    private final PurchaseOrderNumberGenerator numberGenerator;
    
    public Page<PurchaseOrderDto> getAllPurchaseOrders(Pageable pageable) {
        log.debug("Getting all purchase orders with pagination: {}", pageable);
        return purchaseOrderRepository.findAll(pageable)
                .map(purchaseOrderMapper::toDto);
    }
    
    public Optional<PurchaseOrderDto> getPurchaseOrderById(Long id) {
        log.debug("Getting purchase order by id: {}", id);
        return purchaseOrderRepository.findById(id)
                .map(purchaseOrderMapper::toDto);
    }
    
    public Optional<PurchaseOrderDto> getPurchaseOrderByNumber(String poNumber) {
        log.debug("Getting purchase order by number: {}", poNumber);
        return purchaseOrderRepository.findByPoNumber(poNumber)
                .map(purchaseOrderMapper::toDto);
    }
    
    public List<PurchaseOrderDto> getPurchaseOrdersBySupplier(Long supplierId) {
        log.debug("Getting purchase orders by supplier: {}", supplierId);
        return purchaseOrderRepository.findBySupplierId(supplierId)
                .stream()
                .map(purchaseOrderMapper::toDto)
                .toList();
    }
    
    public List<PurchaseOrderDto> getPurchaseOrdersByStatus(PurchaseOrderStatus status) {
        log.debug("Getting purchase orders by status: {}", status);
        return purchaseOrderRepository.findByStatus(status)
                .stream()
                .map(purchaseOrderMapper::toDto)
                .toList();
    }
    
    public Page<PurchaseOrderDto> searchPurchaseOrders(
            String poNumber, Long supplierId, PurchaseOrderStatus status,
            LocalDate orderDateFrom, LocalDate orderDateTo,
            BigDecimal amountFrom, BigDecimal amountTo,
            Pageable pageable) {
        log.debug("Searching purchase orders with filters");
        return purchaseOrderRepository.findByFilters(
                poNumber, supplierId, status, orderDateFrom, orderDateTo,
                amountFrom, amountTo, pageable)
                .map(purchaseOrderMapper::toDto);
    }
    
    @Transactional
    public PurchaseOrderDto createPurchaseOrder(PurchaseOrderDto purchaseOrderDto) {
        log.info("Creating new purchase order");
        
        validatePurchaseOrderForCreation(purchaseOrderDto);
        
        PurchaseOrder purchaseOrder = purchaseOrderMapper.toEntity(purchaseOrderDto);
        
        // Generate PO number if not provided
        if (purchaseOrder.getPoNumber() == null || purchaseOrder.getPoNumber().isEmpty()) {
            purchaseOrder.setPoNumber(numberGenerator.generatePoNumber());
        }
        
        // Set creation metadata
        purchaseOrder.setOrderDate(LocalDate.now());
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        
        // Calculate totals
        purchaseOrder.calculateTotals();
        
        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);
        
        // Publish creation event
        eventService.publishPurchaseOrderCreated(savedOrder);
        
        log.info("Purchase order created successfully with number: {}", savedOrder.getPoNumber());
        return purchaseOrderMapper.toDto(savedOrder);
    }
    
    @Transactional
    public PurchaseOrderDto updatePurchaseOrder(Long id, PurchaseOrderDto purchaseOrderDto) {
        log.info("Updating purchase order with id: {}", id);
        
        PurchaseOrder existingOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with id: " + id));
        
        validatePurchaseOrderForUpdate(existingOrder, purchaseOrderDto);
        
        PurchaseOrder updatedOrder = purchaseOrderMapper.updateEntity(existingOrder, purchaseOrderDto);
        updatedOrder.calculateTotals();
        
        PurchaseOrder savedOrder = purchaseOrderRepository.save(updatedOrder);
        
        // Publish update event
        eventService.publishPurchaseOrderUpdated(savedOrder);
        
        log.info("Purchase order updated successfully: {}", savedOrder.getPoNumber());
        return purchaseOrderMapper.toDto(savedOrder);
    }
    
    @Transactional
    public void deletePurchaseOrder(Long id) {
        log.info("Deleting purchase order with id: {}", id);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with id: " + id));
        
        if (!canDeletePurchaseOrder(purchaseOrder)) {
            throw new RuntimeException("Cannot delete purchase order in current status: " + purchaseOrder.getStatus());
        }
        
        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(purchaseOrder);
        
        // Publish deletion event
        eventService.publishPurchaseOrderDeleted(purchaseOrder);
        
        log.info("Purchase order deleted successfully: {}", purchaseOrder.getPoNumber());
    }
    
    @Transactional
    public PurchaseOrderDto submitForApproval(Long id) {
        log.info("Submitting purchase order for approval: {}", id);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with id: " + id));
        
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new RuntimeException("Purchase order must be in DRAFT status to submit for approval");
        }
        
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);
        
        // Start approval workflow
        approvalWorkflowService.startApprovalProcess(savedOrder);
        
        // Send notification
        emailNotificationService.sendApprovalRequestNotification(savedOrder);
        
        // Publish event
        eventService.publishPurchaseOrderSubmittedForApproval(savedOrder);
        
        log.info("Purchase order submitted for approval: {}", savedOrder.getPoNumber());
        return purchaseOrderMapper.toDto(savedOrder);
    }
    
    @Transactional
    public PurchaseOrderDto approvePurchaseOrder(Long id, Long approverUserId, String comments) {
        log.info("Approving purchase order: {} by user: {}", id, approverUserId);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with id: " + id));
        
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Purchase order must be pending approval");
        }
        
        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        purchaseOrder.setApprovedByUserId(approverUserId);
        purchaseOrder.setApprovedDate(LocalDateTime.now());
        
        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);
        
        // Send to supplier
        sendToSupplier(savedOrder);
        
        // Send notification
        emailNotificationService.sendApprovalNotification(savedOrder, comments);
        
        // Publish event
        eventService.publishPurchaseOrderApproved(savedOrder);
        
        log.info("Purchase order approved: {}", savedOrder.getPoNumber());
        return purchaseOrderMapper.toDto(savedOrder);
    }
    
    @Transactional
    public PurchaseOrderDto rejectPurchaseOrder(Long id, Long rejectorUserId, String rejectionReason) {
        log.info("Rejecting purchase order: {} by user: {}", id, rejectorUserId);
        
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with id: " + id));
        
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING_APPROVAL) {
            throw new RuntimeException("Purchase order must be pending approval");
        }
        
        purchaseOrder.setStatus(PurchaseOrderStatus.DRAFT);
        
        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);
        
        // Send notification
        emailNotificationService.sendRejectionNotification(savedOrder, rejectionReason);
        
        // Publish event
        eventService.publishPurchaseOrderRejected(savedOrder, rejectionReason);
        
        log.info("Purchase order rejected: {}", savedOrder.getPoNumber());
        return purchaseOrderMapper.toDto(savedOrder);
    }
    
    @Transactional
    public PurchaseOrderDto sendToSupplier(Long id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase order not found with id: " + id));
        
        return sendToSupplier(purchaseOrder);
    }
    
    private PurchaseOrderDto sendToSupplier(PurchaseOrder purchaseOrder) {
        log.info("Sending purchase order to supplier: {}", purchaseOrder.getPoNumber());
        
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.APPROVED) {
            throw new RuntimeException("Purchase order must be approved before sending to supplier");
        }
        
        purchaseOrder.setStatus(PurchaseOrderStatus.SENT_TO_SUPPLIER);
        purchaseOrder.setSentToSupplierDate(LocalDateTime.now());
        
        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);
        
        // Send email to supplier
        emailNotificationService.sendPurchaseOrderToSupplier(savedOrder);
        
        // Publish event
        eventService.publishPurchaseOrderSentToSupplier(savedOrder);
        
        log.info("Purchase order sent to supplier: {}", savedOrder.getPoNumber());
        return purchaseOrderMapper.toDto(savedOrder);
    }
    
    public List<PurchaseOrderDto> getOverduePurchaseOrders() {
        log.debug("Getting overdue purchase orders");
        return purchaseOrderRepository.findOverdueOrders(LocalDate.now())
                .stream()
                .map(purchaseOrderMapper::toDto)
                .toList();
    }
    
    public BigDecimal getTotalPurchaseValue(LocalDate fromDate, LocalDate toDate) {
        return purchaseOrderRepository.getTotalPurchaseValue(fromDate, toDate);
    }
    
    public Long getPurchaseOrderCount() {
        return purchaseOrderRepository.count();
    }
    
    private void validatePurchaseOrderForCreation(PurchaseOrderDto purchaseOrderDto) {
        if (purchaseOrderDto.getSupplierId() == null) {
            throw new RuntimeException("Supplier is required");
        }
        
        if (purchaseOrderDto.getItems() == null || purchaseOrderDto.getItems().isEmpty()) {
            throw new RuntimeException("Purchase order must have at least one item");
        }
        
        // Validate supplier exists and is active
        if (!supplierService.isSupplierActiveAndValid(purchaseOrderDto.getSupplierId())) {
            throw new RuntimeException("Supplier is not active or valid");
        }
    }
    
    private void validatePurchaseOrderForUpdate(PurchaseOrder existingOrder, PurchaseOrderDto updates) {
        if (existingOrder.getStatus() == PurchaseOrderStatus.COMPLETED ||
            existingOrder.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new RuntimeException("Cannot update completed or cancelled purchase order");
        }
    }
    
    private boolean canDeletePurchaseOrder(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getStatus() == PurchaseOrderStatus.DRAFT ||
               purchaseOrder.getStatus() == PurchaseOrderStatus.PENDING_APPROVAL;
    }
}