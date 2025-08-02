package com.erp.system.finance.repository;

import com.erp.system.finance.entity.Invoice;
import com.erp.system.finance.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    List<Invoice> findByStatus(InvoiceStatus status);
    
    List<Invoice> findByCustomerId(Long customerId);
    
    @Query("SELECT i FROM Invoice i WHERE i.dueDate < :currentDate AND i.status = 'SENT'")
    List<Invoice> findOverdueInvoices(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT i FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate ORDER BY i.invoiceDate DESC")
    List<Invoice> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.status = 'PAID' AND i.invoiceDate BETWEEN :startDate AND :endDate")
    Double getTotalRevenueByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(i.balanceDue) FROM Invoice i WHERE i.status IN ('SENT', 'OVERDUE', 'PARTIALLY_PAID')")
    Double getTotalOutstandingAmount();
    
    Page<Invoice> findByActiveTrue(Pageable pageable);
    
    boolean existsByInvoiceNumber(String invoiceNumber);
}