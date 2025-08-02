package com.erp.system.hr.controller;

import com.erp.system.hr.dto.EmployeeDto;
import com.erp.system.hr.enums.EmployeeStatus;
import com.erp.system.hr.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hr/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {
    
    private final EmployeeService employeeService;
    
    @GetMapping
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(Pageable pageable) {
        log.info("GET /api/hr/employees - Getting all employees");
        Page<EmployeeDto> employees = employeeService.getAllEmployees(pageable);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        log.info("GET /api/hr/employees/{} - Getting employee by id", id);
        return employeeService.getEmployeeById(id)
                .map(employee -> ResponseEntity.ok(employee))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{code}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> getEmployeeByCode(@PathVariable String code) {
        log.info("GET /api/hr/employees/code/{} - Getting employee by code", code);
        return employeeService.getEmployeeByCode(code)
                .map(employee -> ResponseEntity.ok(employee))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> getEmployeeByEmail(@PathVariable String email) {
        log.info("GET /api/hr/employees/email/{} - Getting employee by email", email);
        return employeeService.getEmployeeByEmail(email)
                .map(employee -> ResponseEntity.ok(employee))
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByDepartment(@PathVariable Long departmentId) {
        log.info("GET /api/hr/employees/department/{} - Getting employees by department", departmentId);
        List<EmployeeDto> employees = employeeService.getEmployeesByDepartment(departmentId);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getEmployeesByStatus(@PathVariable EmployeeStatus status) {
        log.info("GET /api/hr/employees/status/{} - Getting employees by status", status);
        List<EmployeeDto> employees = employeeService.getEmployeesByStatus(status);
        return ResponseEntity.ok(employees);
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<Page<EmployeeDto>> searchEmployees(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) EmployeeStatus status,
            Pageable pageable) {
        log.info("GET /api/hr/employees/search - Searching employees with filters");
        Page<EmployeeDto> employees = employeeService.searchEmployees(
                firstName, lastName, email, departmentId, positionId, status, pageable);
        return ResponseEntity.ok(employees);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
        log.info("POST /api/hr/employees - Creating new employee");
        EmployeeDto createdEmployee = employeeService.createEmployee(employeeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<EmployeeDto> updateEmployee(
            @PathVariable Long id, 
            @Valid @RequestBody EmployeeDto employeeDto) {
        log.info("PUT /api/hr/employees/{} - Updating employee", id);
        EmployeeDto updatedEmployee = employeeService.updateEmployee(id, employeeDto);
        return ResponseEntity.ok(updatedEmployee);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        log.info("DELETE /api/hr/employees/{} - Deleting employee", id);
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{managerId}/subordinates")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeDto>> getSubordinates(@PathVariable Long managerId) {
        log.info("GET /api/hr/employees/{}/subordinates - Getting subordinates", managerId);
        List<EmployeeDto> subordinates = employeeService.getSubordinates(managerId);
        return ResponseEntity.ok(subordinates);
    }
    
    @GetMapping("/stats/count-by-status/{status}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<Long> getEmployeeCountByStatus(@PathVariable EmployeeStatus status) {
        log.info("GET /api/hr/employees/stats/count-by-status/{} - Getting count by status", status);
        Long count = employeeService.getEmployeeCountByStatus(status);
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/stats/count-by-department/{departmentId}")
    @PreAuthorize("hasRole('HR_MANAGER') or hasRole('HR_EMPLOYEE') or hasRole('ADMIN')")
    public ResponseEntity<Long> getEmployeeCountByDepartment(@PathVariable Long departmentId) {
        log.info("GET /api/hr/employees/stats/count-by-department/{} - Getting count by department", departmentId);
        Long count = employeeService.getEmployeeCountByDepartment(departmentId);
        return ResponseEntity.ok(count);
    }
}