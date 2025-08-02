package com.erp.system.inventory.repository;

import com.erp.system.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByCode(String code);
    
    List<Category> findByParentCategoryIsNull();
    
    List<Category> findByParentCategoryId(Long parentCategoryId);
    
    @Query("SELECT c FROM Category c WHERE c.level = :level AND c.active = true ORDER BY c.name")
    List<Category> findByLevel(@Param("level") Integer level);
    
    @Query("SELECT c FROM Category c WHERE c.name LIKE %:name% AND c.active = true")
    List<Category> findByNameContaining(@Param("name") String name);
    
    boolean existsByCode(String code);
    
    boolean existsByName(String name);
}