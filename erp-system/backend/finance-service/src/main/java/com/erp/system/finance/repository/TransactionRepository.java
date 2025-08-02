package com.erp.system.finance.repository;

import com.erp.system.finance.entity.Transaction;
import com.erp.system.finance.enums.TransactionType;
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
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    Optional<Transaction> findByTransactionNumber(String transactionNumber);
    
    List<Transaction> findByTransactionType(TransactionType transactionType);
    
    @Query("SELECT t FROM Transaction t WHERE t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate DESC")
    List<Transaction> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE t.customerId = :customerId ORDER BY t.transactionDate DESC")
    List<Transaction> findByCustomerId(@Param("customerId") Long customerId);
    
    @Query("SELECT t FROM Transaction t WHERE t.supplierId = :supplierId ORDER BY t.transactionDate DESC")
    List<Transaction> findBySupplierId(@Param("supplierId") Long supplierId);
    
    Page<Transaction> findByActiveTrue(Pageable pageable);
    
    boolean existsByTransactionNumber(String transactionNumber);
}