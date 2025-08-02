package com.erp.system.hr.repository;

import com.erp.system.hr.entity.LeaveRequest;
import com.erp.system.hr.enums.LeaveRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByStatus(LeaveRequestStatus status);
    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId AND " +
           "lr.status = :status")
    List<LeaveRequest> findByEmployeeIdAndStatus(
        @Param("employeeId") Long employeeId, 
        @Param("status") LeaveRequestStatus status
    );
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.startDate <= :endDate AND lr.endDate >= :startDate AND " +
           "lr.employee.id = :employeeId AND lr.status = 'APPROVED'")
    List<LeaveRequest> findOverlappingLeaves(
        @Param("employeeId") Long employeeId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.employee.manager.id = :managerId AND lr.status = 'PENDING'")
    List<LeaveRequest> findPendingRequestsForManager(@Param("managerId") Long managerId);
    
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE " +
           "lr.employee.id = :employeeId AND lr.status = 'APPROVED' AND " +
           "YEAR(lr.startDate) = :year")
    Long countApprovedLeavesByEmployeeAndYear(
        @Param("employeeId") Long employeeId, 
        @Param("year") int year
    );
}