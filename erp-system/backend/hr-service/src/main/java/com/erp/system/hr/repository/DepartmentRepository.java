package com.erp.system.hr.repository;

import com.erp.system.hr.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    Optional<Department> findByCode(String code);
    List<Department> findByIsActive(Boolean isActive);
    List<Department> findByParentDepartmentId(Long parentDepartmentId);
    List<Department> findByParentDepartmentIsNull();
    
    @Query("SELECT d FROM Department d WHERE " +
           "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:isActive IS NULL OR d.isActive = :isActive)")
    List<Department> findByFilters(
        @Param("name") String name,
        @Param("isActive") Boolean isActive
    );
    
    boolean existsByCode(String code);
}