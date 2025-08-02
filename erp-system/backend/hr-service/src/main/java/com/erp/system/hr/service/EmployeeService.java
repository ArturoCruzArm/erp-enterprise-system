package com.erp.system.hr.service;

import com.erp.system.hr.dto.EmployeeDto;
import com.erp.system.hr.entity.Employee;
import com.erp.system.hr.enums.EmployeeStatus;
import com.erp.system.hr.mapper.EmployeeMapper;
import com.erp.system.hr.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final EmployeeEventService employeeEventService;
    
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        log.debug("Getting all employees with pagination: {}", pageable);
        return employeeRepository.findAll(pageable)
                .map(employeeMapper::toDto);
    }
    
    public Optional<EmployeeDto> getEmployeeById(Long id) {
        log.debug("Getting employee by id: {}", id);
        return employeeRepository.findById(id)
                .map(employeeMapper::toDto);
    }
    
    public Optional<EmployeeDto> getEmployeeByCode(String code) {
        log.debug("Getting employee by code: {}", code);
        return employeeRepository.findByEmployeeCode(code)
                .map(employeeMapper::toDto);
    }
    
    public Optional<EmployeeDto> getEmployeeByEmail(String email) {
        log.debug("Getting employee by email: {}", email);
        return employeeRepository.findByEmail(email)
                .map(employeeMapper::toDto);
    }
    
    public List<EmployeeDto> getEmployeesByDepartment(Long departmentId) {
        log.debug("Getting employees by department: {}", departmentId);
        return employeeRepository.findByDepartmentId(departmentId)
                .stream()
                .map(employeeMapper::toDto)
                .toList();
    }
    
    public List<EmployeeDto> getEmployeesByStatus(EmployeeStatus status) {
        log.debug("Getting employees by status: {}", status);
        return employeeRepository.findByStatus(status)
                .stream()
                .map(employeeMapper::toDto)
                .toList();
    }
    
    public Page<EmployeeDto> searchEmployees(
            String firstName, String lastName, String email,
            Long departmentId, Long positionId, EmployeeStatus status,
            Pageable pageable) {
        log.debug("Searching employees with filters");
        return employeeRepository.findByFilters(
                firstName, lastName, email, departmentId, positionId, status, pageable)
                .map(employeeMapper::toDto);
    }
    
    @Transactional
    public EmployeeDto createEmployee(EmployeeDto employeeDto) {
        log.info("Creating new employee: {}", employeeDto.getEmployeeCode());
        
        validateEmployeeForCreation(employeeDto);
        
        Employee employee = employeeMapper.toEntity(employeeDto);
        Employee savedEmployee = employeeRepository.save(employee);
        
        // Publish employee created event
        employeeEventService.publishEmployeeCreated(savedEmployee);
        
        log.info("Employee created successfully with id: {}", savedEmployee.getId());
        return employeeMapper.toDto(savedEmployee);
    }
    
    @Transactional
    public EmployeeDto updateEmployee(Long id, EmployeeDto employeeDto) {
        log.info("Updating employee with id: {}", id);
        
        Employee existingEmployee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        validateEmployeeForUpdate(id, employeeDto);
        
        Employee updatedEmployee = employeeMapper.updateEntity(existingEmployee, employeeDto);
        Employee savedEmployee = employeeRepository.save(updatedEmployee);
        
        // Publish employee updated event
        employeeEventService.publishEmployeeUpdated(savedEmployee);
        
        log.info("Employee updated successfully with id: {}", savedEmployee.getId());
        return employeeMapper.toDto(savedEmployee);
    }
    
    @Transactional
    public void deleteEmployee(Long id) {
        log.info("Deleting employee with id: {}", id);
        
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        
        // Soft delete - change status to terminated
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);
        
        // Publish employee deleted event
        employeeEventService.publishEmployeeDeleted(employee);
        
        log.info("Employee deleted successfully with id: {}", id);
    }
    
    public Long getEmployeeCountByStatus(EmployeeStatus status) {
        return employeeRepository.countByStatus(status);
    }
    
    public Long getEmployeeCountByDepartment(Long departmentId) {
        return employeeRepository.countByDepartmentId(departmentId);
    }
    
    public List<EmployeeDto> getSubordinates(Long managerId) {
        log.debug("Getting subordinates for manager: {}", managerId);
        return employeeRepository.findSubordinates(managerId)
                .stream()
                .map(employeeMapper::toDto)
                .toList();
    }
    
    private void validateEmployeeForCreation(EmployeeDto employeeDto) {
        if (employeeRepository.existsByEmployeeCode(employeeDto.getEmployeeCode())) {
            throw new RuntimeException("Employee code already exists: " + employeeDto.getEmployeeCode());
        }
        
        if (employeeRepository.existsByEmail(employeeDto.getEmail())) {
            throw new RuntimeException("Email already exists: " + employeeDto.getEmail());
        }
        
        if (employeeDto.getNationalId() != null && 
            employeeRepository.existsByNationalId(employeeDto.getNationalId())) {
            throw new RuntimeException("National ID already exists: " + employeeDto.getNationalId());
        }
    }
    
    private void validateEmployeeForUpdate(Long id, EmployeeDto employeeDto) {
        // Check if email is being changed and if it already exists
        Optional<Employee> existingByEmail = employeeRepository.findByEmail(employeeDto.getEmail());
        if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(id)) {
            throw new RuntimeException("Email already exists: " + employeeDto.getEmail());
        }
        
        // Check if national ID is being changed and if it already exists
        if (employeeDto.getNationalId() != null) {
            Optional<Employee> existingByNationalId = employeeRepository.findByNationalId(employeeDto.getNationalId());
            if (existingByNationalId.isPresent() && !existingByNationalId.get().getId().equals(id)) {
                throw new RuntimeException("National ID already exists: " + employeeDto.getNationalId());
            }
        }
    }
}