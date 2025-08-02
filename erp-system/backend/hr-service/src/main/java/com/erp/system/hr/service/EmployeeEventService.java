package com.erp.system.hr.service;

import com.erp.system.hr.entity.Employee;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeEventService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String EMPLOYEE_TOPIC = "employee-events";
    
    public void publishEmployeeCreated(Employee employee) {
        try {
            Map<String, Object> event = createEmployeeEvent("EMPLOYEE_CREATED", employee);
            kafkaTemplate.send(EMPLOYEE_TOPIC, employee.getId().toString(), event);
            log.info("Published employee created event for employee: {}", employee.getId());
        } catch (Exception e) {
            log.error("Error publishing employee created event", e);
        }
    }
    
    public void publishEmployeeUpdated(Employee employee) {
        try {
            Map<String, Object> event = createEmployeeEvent("EMPLOYEE_UPDATED", employee);
            kafkaTemplate.send(EMPLOYEE_TOPIC, employee.getId().toString(), event);
            log.info("Published employee updated event for employee: {}", employee.getId());
        } catch (Exception e) {
            log.error("Error publishing employee updated event", e);
        }
    }
    
    public void publishEmployeeDeleted(Employee employee) {
        try {
            Map<String, Object> event = createEmployeeEvent("EMPLOYEE_DELETED", employee);
            kafkaTemplate.send(EMPLOYEE_TOPIC, employee.getId().toString(), event);
            log.info("Published employee deleted event for employee: {}", employee.getId());
        } catch (Exception e) {
            log.error("Error publishing employee deleted event", e);
        }
    }
    
    private Map<String, Object> createEmployeeEvent(String eventType, Employee employee) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", eventType);
        event.put("timestamp", System.currentTimeMillis());
        event.put("employeeId", employee.getId());
        event.put("employeeCode", employee.getEmployeeCode());
        event.put("employeeName", employee.getFullName());
        event.put("email", employee.getEmail());
        event.put("status", employee.getStatus().toString());
        
        if (employee.getDepartment() != null) {
            event.put("departmentId", employee.getDepartment().getId());
            event.put("departmentName", employee.getDepartment().getName());
        }
        
        if (employee.getPosition() != null) {
            event.put("positionId", employee.getPosition().getId());
            event.put("positionTitle", employee.getPosition().getTitle());
        }
        
        return event;
    }
}