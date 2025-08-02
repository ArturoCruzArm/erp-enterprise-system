package com.erp.system.hr.mapper;

import com.erp.system.hr.dto.EmployeeDto;
import com.erp.system.hr.entity.Employee;
import org.springframework.stereotype.Component;

@Component
public class EmployeeMapper {
    
    public EmployeeDto toDto(Employee employee) {
        if (employee == null) {
            return null;
        }
        
        EmployeeDto dto = new EmployeeDto();
        dto.setId(employee.getId());
        dto.setEmployeeCode(employee.getEmployeeCode());
        dto.setFirstName(employee.getFirstName());
        dto.setLastName(employee.getLastName());
        dto.setEmail(employee.getEmail());
        dto.setPhone(employee.getPhone());
        dto.setNationalId(employee.getNationalId());
        dto.setDateOfBirth(employee.getDateOfBirth());
        dto.setGender(employee.getGender());
        dto.setAddress(employee.getAddress());
        dto.setCity(employee.getCity());
        dto.setCountry(employee.getCountry());
        dto.setPostalCode(employee.getPostalCode());
        dto.setHireDate(employee.getHireDate());
        dto.setTerminationDate(employee.getTerminationDate());
        dto.setStatus(employee.getStatus());
        dto.setSalary(employee.getSalary());
        dto.setEmergencyContactName(employee.getEmergencyContactName());
        dto.setEmergencyContactPhone(employee.getEmergencyContactPhone());
        dto.setBankAccountNumber(employee.getBankAccountNumber());
        dto.setTaxId(employee.getTaxId());
        dto.setProfileImageUrl(employee.getProfileImageUrl());
        dto.setNotes(employee.getNotes());
        dto.setCreatedAt(employee.getCreatedAt());
        dto.setUpdatedAt(employee.getUpdatedAt());
        
        // Department info
        if (employee.getDepartment() != null) {
            dto.setDepartmentId(employee.getDepartment().getId());
            dto.setDepartmentName(employee.getDepartment().getName());
        }
        
        // Position info
        if (employee.getPosition() != null) {
            dto.setPositionId(employee.getPosition().getId());
            dto.setPositionTitle(employee.getPosition().getTitle());
        }
        
        // Manager info
        if (employee.getManager() != null) {
            dto.setManagerId(employee.getManager().getId());
            dto.setManagerName(employee.getManager().getFullName());
        }
        
        return dto;
    }
    
    public Employee toEntity(EmployeeDto dto) {
        if (dto == null) {
            return null;
        }
        
        Employee employee = new Employee();
        employee.setEmployeeCode(dto.getEmployeeCode());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setNationalId(dto.getNationalId());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setGender(dto.getGender());
        employee.setAddress(dto.getAddress());
        employee.setCity(dto.getCity());
        employee.setCountry(dto.getCountry());
        employee.setPostalCode(dto.getPostalCode());
        employee.setHireDate(dto.getHireDate());
        employee.setTerminationDate(dto.getTerminationDate());
        employee.setStatus(dto.getStatus());
        employee.setSalary(dto.getSalary());
        employee.setEmergencyContactName(dto.getEmergencyContactName());
        employee.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        employee.setBankAccountNumber(dto.getBankAccountNumber());
        employee.setTaxId(dto.getTaxId());
        employee.setProfileImageUrl(dto.getProfileImageUrl());
        employee.setNotes(dto.getNotes());
        
        return employee;
    }
    
    public Employee updateEntity(Employee employee, EmployeeDto dto) {
        if (dto == null) {
            return employee;
        }
        
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setNationalId(dto.getNationalId());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setGender(dto.getGender());
        employee.setAddress(dto.getAddress());
        employee.setCity(dto.getCity());
        employee.setCountry(dto.getCountry());
        employee.setPostalCode(dto.getPostalCode());
        employee.setTerminationDate(dto.getTerminationDate());
        employee.setStatus(dto.getStatus());
        employee.setSalary(dto.getSalary());
        employee.setEmergencyContactName(dto.getEmergencyContactName());
        employee.setEmergencyContactPhone(dto.getEmergencyContactPhone());
        employee.setBankAccountNumber(dto.getBankAccountNumber());
        employee.setTaxId(dto.getTaxId());
        employee.setProfileImageUrl(dto.getProfileImageUrl());
        employee.setNotes(dto.getNotes());
        
        return employee;
    }
}