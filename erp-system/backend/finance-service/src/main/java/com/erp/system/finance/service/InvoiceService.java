package com.erp.system.finance.service;

import com.erp.system.finance.dto.InvoiceDto;
import com.erp.system.finance.dto.InvoiceItemDto;
import com.erp.system.finance.entity.Invoice;
import com.erp.system.finance.entity.InvoiceItem;
import com.erp.system.finance.enums.InvoiceStatus;
import com.erp.system.finance.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    
    @Transactional
    public InvoiceDto createInvoice(InvoiceDto invoiceDto) {
        Invoice invoice = mapToEntity(invoiceDto);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setStatus(InvoiceStatus.DRAFT);
        
        calculateTotals(invoice);
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Created invoice: {}", savedInvoice.getInvoiceNumber());
        
        return mapToDto(savedInvoice);
    }
    
    @Transactional
    public InvoiceDto updateInvoice(Long id, InvoiceDto invoiceDto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        updateInvoiceFromDto(invoice, invoiceDto);
        calculateTotals(invoice);
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Updated invoice: {}", savedInvoice.getInvoiceNumber());
        
        return mapToDto(savedInvoice);
    }
    
    public InvoiceDto getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        return mapToDto(invoice);
    }
    
    public Page<InvoiceDto> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findByActiveTrue(pageable)
                .map(this::mapToDto);
    }
    
    public List<InvoiceDto> getInvoicesByCustomer(Long customerId) {
        return invoiceRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    public List<InvoiceDto> getOverdueInvoices() {
        return invoiceRepository.findOverdueInvoices(LocalDate.now())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void markInvoiceAsSent(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        invoice.setStatus(InvoiceStatus.SENT);
        invoiceRepository.save(invoice);
        log.info("Invoice {} marked as sent", invoice.getInvoiceNumber());
    }
    
    @Transactional
    public void markInvoiceAsPaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));
        
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAmount(invoice.getTotalAmount());
        invoice.setBalanceDue(BigDecimal.ZERO);
        invoiceRepository.save(invoice);
        log.info("Invoice {} marked as paid", invoice.getInvoiceNumber());
    }
    
    private void calculateTotals(Invoice invoice) {
        BigDecimal subtotal = invoice.getItems().stream()
                .map(InvoiceItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal taxAmount = invoice.getItems().stream()
                .map(InvoiceItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal discountAmount = invoice.getItems().stream()
                .map(InvoiceItem::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setDiscountAmount(discountAmount);
        invoice.setTotalAmount(subtotal.add(taxAmount).subtract(discountAmount));
        invoice.setBalanceDue(invoice.getTotalAmount().subtract(invoice.getPaidAmount()));
    }
    
    private String generateInvoiceNumber() {
        String prefix = "INV-" + LocalDate.now().getYear() + "-";
        long count = invoiceRepository.count() + 1;
        return prefix + String.format("%06d", count);
    }
    
    private Invoice mapToEntity(InvoiceDto dto) {
        Invoice invoice = new Invoice();
        updateInvoiceFromDto(invoice, dto);
        return invoice;
    }
    
    private void updateInvoiceFromDto(Invoice invoice, InvoiceDto dto) {
        invoice.setInvoiceDate(dto.getInvoiceDate());
        invoice.setDueDate(dto.getDueDate());
        invoice.setCustomerId(dto.getCustomerId());
        invoice.setCustomerName(dto.getCustomerName());
        invoice.setBillingAddress(dto.getBillingAddress());
        invoice.setNotes(dto.getNotes());
        invoice.setCurrencyCode(dto.getCurrencyCode());
        
        if (dto.getItems() != null) {
            List<InvoiceItem> items = dto.getItems().stream()
                    .map(itemDto -> mapToInvoiceItem(itemDto, invoice))
                    .collect(Collectors.toList());
            invoice.setItems(items);
        }
    }
    
    private InvoiceItem mapToInvoiceItem(InvoiceItemDto dto, Invoice invoice) {
        InvoiceItem item = new InvoiceItem();
        item.setInvoice(invoice);
        item.setProductId(dto.getProductId());
        item.setItemDescription(dto.getItemDescription());
        item.setQuantity(dto.getQuantity());
        item.setUnitPrice(dto.getUnitPrice());
        item.setDiscountPercentage(dto.getDiscountPercentage());
        item.setTaxPercentage(dto.getTaxPercentage());
        item.setLineOrder(dto.getLineOrder());
        
        // Calculate amounts
        BigDecimal lineSubtotal = dto.getQuantity().multiply(dto.getUnitPrice());
        BigDecimal discountAmount = lineSubtotal.multiply(dto.getDiscountPercentage()).divide(BigDecimal.valueOf(100));
        BigDecimal taxableAmount = lineSubtotal.subtract(discountAmount);
        BigDecimal taxAmount = taxableAmount.multiply(dto.getTaxPercentage()).divide(BigDecimal.valueOf(100));
        
        item.setDiscountAmount(discountAmount);
        item.setTaxAmount(taxAmount);
        item.setLineTotal(taxableAmount.add(taxAmount));
        
        return item;
    }
    
    private InvoiceDto mapToDto(Invoice invoice) {
        InvoiceDto dto = new InvoiceDto();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setInvoiceDate(invoice.getInvoiceDate());
        dto.setDueDate(invoice.getDueDate());
        dto.setStatus(invoice.getStatus());
        dto.setCustomerId(invoice.getCustomerId());
        dto.setCustomerName(invoice.getCustomerName());
        dto.setBillingAddress(invoice.getBillingAddress());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setTaxAmount(invoice.getTaxAmount());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTotalAmount(invoice.getTotalAmount());
        dto.setPaidAmount(invoice.getPaidAmount());
        dto.setBalanceDue(invoice.getBalanceDue());
        dto.setNotes(invoice.getNotes());
        dto.setCurrencyCode(invoice.getCurrencyCode());
        
        if (invoice.getItems() != null) {
            List<InvoiceItemDto> items = invoice.getItems().stream()
                    .map(this::mapToInvoiceItemDto)
                    .collect(Collectors.toList());
            dto.setItems(items);
        }
        
        return dto;
    }
    
    private InvoiceItemDto mapToInvoiceItemDto(InvoiceItem item) {
        InvoiceItemDto dto = new InvoiceItemDto();
        dto.setId(item.getId());
        dto.setProductId(item.getProductId());
        dto.setItemDescription(item.getItemDescription());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setDiscountPercentage(item.getDiscountPercentage());
        dto.setDiscountAmount(item.getDiscountAmount());
        dto.setTaxPercentage(item.getTaxPercentage());
        dto.setTaxAmount(item.getTaxAmount());
        dto.setLineTotal(item.getLineTotal());
        dto.setLineOrder(item.getLineOrder());
        return dto;
    }
}