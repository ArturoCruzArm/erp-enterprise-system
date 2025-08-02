package com.erp.system.hr.repository;

import com.erp.system.hr.entity.Employee;
import com.erp.system.hr.enums.EmployeeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmployeeCode(String employeeCode);
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByNationalId(String nationalId);
    
    List<Employee> findByStatus(EmployeeStatus status);
    List<Employee> findByDepartmentId(Long departmentId);
    List<Employee> findByPositionId(Long positionId);
    List<Employee> findByManagerId(Long managerId);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "(:firstName IS NULL OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) AND " +
           "(:lastName IS NULL OR LOWER(e.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) AND " +
           "(:email IS NULL OR LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
           "(:departmentId IS NULL OR e.department.id = :departmentId) AND " +
           "(:positionId IS NULL OR e.position.id = :positionId) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<Employee> findByFilters(
        @Param("firstName") String firstName,
        @Param("lastName") String lastName,
        @Param("email") String email,
        @Param("departmentId") Long departmentId,
        @Param("positionId") Long positionId,
        @Param("status") EmployeeStatus status,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = :status")
    Long countByStatus(@Param("status") EmployeeStatus status);
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department.id = :departmentId")
    Long countByDepartmentId(@Param("departmentId") Long departmentId);
    
    @Query("SELECT e FROM Employee e WHERE e.manager.id = :managerId")
    List<Employee> findSubordinates(@Param("managerId") Long managerId);
    
    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByEmail(String email);
    boolean existsByNationalId(String nationalId);
}