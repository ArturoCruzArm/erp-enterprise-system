package com.erp.system.finance.controller;

import com.erp.system.finance.dto.InvoiceDto;
import com.erp.system.finance.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @PostMapping
    public ResponseEntity<InvoiceDto> createInvoice(@Valid @RequestBody InvoiceDto invoiceDto) {
        InvoiceDto createdInvoice = invoiceService.createInvoice(invoiceDto);
        return new ResponseEntity<>(createdInvoice, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceDto> updateInvoice(@PathVariable Long id, @Valid @RequestBody InvoiceDto invoiceDto) {
        InvoiceDto updatedInvoice = invoiceService.updateInvoice(id, invoiceDto);
        return ResponseEntity.ok(updatedInvoice);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable Long id) {
        InvoiceDto invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }
    
    @GetMapping
    public ResponseEntity<Page<InvoiceDto>> getAllInvoices(Pageable pageable) {
        Page<InvoiceDto> invoices = invoiceService.getAllInvoices(pageable);
        return ResponseEntity.ok(invoices);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<InvoiceDto>> getInvoicesByCustomer(@PathVariable Long customerId) {
        List<InvoiceDto> invoices = invoiceService.getInvoicesByCustomer(customerId);
        return ResponseEntity.ok(invoices);
    }
    
    @GetMapping("/overdue")
    public ResponseEntity<List<InvoiceDto>> getOverdueInvoices() {
        List<InvoiceDto> overdueInvoices = invoiceService.getOverdueInvoices();
        return ResponseEntity.ok(overdueInvoices);
    }
    
    @PutMapping("/{id}/send")
    public ResponseEntity<Void> sendInvoice(@PathVariable Long id) {
        invoiceService.markInvoiceAsSent(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}/paid")
    public ResponseEntity<Void> markInvoiceAsPaid(@PathVariable Long id) {
        invoiceService.markInvoiceAsPaid(id);
        return ResponseEntity.ok().build();
    }
}