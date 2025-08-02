package com.erp.system.finance.repository;

import com.erp.system.finance.entity.ChartOfAccounts;
import com.erp.system.finance.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChartOfAccountsRepository extends JpaRepository<ChartOfAccounts, Long> {
    
    Optional<ChartOfAccounts> findByAccountCode(String accountCode);
    
    List<ChartOfAccounts> findByAccountType(AccountType accountType);
    
    List<ChartOfAccounts> findByParentAccountIsNull();
    
    List<ChartOfAccounts> findByParentAccountId(Long parentAccountId);
    
    @Query("SELECT c FROM ChartOfAccounts c WHERE c.level = :level AND c.active = true ORDER BY c.accountCode")
    List<ChartOfAccounts> findByLevel(@Param("level") Integer level);
    
    @Query("SELECT c FROM ChartOfAccounts c WHERE c.isHeader = false AND c.active = true ORDER BY c.accountCode")
    List<ChartOfAccounts> findDetailAccounts();
    
    boolean existsByAccountCode(String accountCode);
}