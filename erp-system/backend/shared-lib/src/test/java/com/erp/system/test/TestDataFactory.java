package com.erp.system.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TestDataFactory {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // User test data
    public Map<String, Object> createTestUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", UUID.randomUUID().toString());
        user.put("username", "testuser_" + System.currentTimeMillis());
        user.put("email", "test@example.com");
        user.put("firstName", "Test");
        user.put("lastName", "User");
        user.put("active", true);
        user.put("createdAt", LocalDateTime.now());
        return user;
    }

    public Map<String, Object> createTestUserWithRole(String role) {
        Map<String, Object> user = createTestUser();
        user.put("role", role);
        return user;
    }

    // Employee test data
    public Map<String, Object> createTestEmployee() {
        Map<String, Object> employee = new HashMap<>();
        employee.put("id", UUID.randomUUID().toString());
        employee.put("employeeId", "EMP" + System.currentTimeMillis());
        employee.put("firstName", "John");
        employee.put("lastName", "Doe");
        employee.put("email", "john.doe@company.com");
        employee.put("department", "Engineering");
        employee.put("position", "Software Developer");
        employee.put("salary", 75000.00);
        employee.put("hireDate", LocalDateTime.now().minusYears(1));
        employee.put("status", "ACTIVE");
        return employee;
    }

    // Product test data
    public Map<String, Object> createTestProduct() {
        Map<String, Object> product = new HashMap<>();
        product.put("id", UUID.randomUUID().toString());
        product.put("sku", "PRD" + System.currentTimeMillis());
        product.put("name", "Test Product");
        product.put("description", "This is a test product");
        product.put("price", 99.99);
        product.put("category", "Electronics");
        product.put("inStock", true);
        product.put("quantity", 100);
        return product;
    }

    // Financial transaction test data
    public Map<String, Object> createTestTransaction() {
        Map<String, Object> transaction = new HashMap<>();
        transaction.put("id", UUID.randomUUID().toString());
        transaction.put("type", "INCOME");
        transaction.put("amount", 1500.00);
        transaction.put("description", "Test transaction");
        transaction.put("category", "Sales");
        transaction.put("date", LocalDateTime.now());
        transaction.put("reference", "TXN" + System.currentTimeMillis());
        return transaction;
    }

    // Invoice test data
    public Map<String, Object> createTestInvoice() {
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("id", UUID.randomUUID().toString());
        invoice.put("invoiceNumber", "INV" + System.currentTimeMillis());
        invoice.put("customerId", UUID.randomUUID().toString());
        invoice.put("amount", 2500.00);
        invoice.put("tax", 250.00);
        invoice.put("total", 2750.00);
        invoice.put("status", "PENDING");
        invoice.put("issueDate", LocalDateTime.now());
        invoice.put("dueDate", LocalDateTime.now().plusDays(30));
        return invoice;
    }

    // JWT Token for testing
    public String createTestJwtToken(String userId, String role) {
        // This is a simplified JWT token for testing
        // In real implementation, you'd use proper JWT library
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userId);
        claims.put("role", role);
        claims.put("iat", System.currentTimeMillis() / 1000);
        claims.put("exp", (System.currentTimeMillis() / 1000) + 3600); // 1 hour
        
        // This would be properly signed in real implementation
        return "test.jwt.token." + userId;
    }

    // Performance test data
    public Map<String, Object> createLargeDataset(int size) {
        Map<String, Object> dataset = new HashMap<>();
        for (int i = 0; i < size; i++) {
            dataset.put("item_" + i, createTestProduct());
        }
        return dataset;
    }

    // Error scenarios
    public Map<String, Object> createInvalidUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("username", ""); // Invalid empty username
        user.put("email", "invalid-email"); // Invalid email format
        user.put("firstName", null); // Null first name
        return user;
    }

    public Map<String, Object> createInvalidProduct() {
        Map<String, Object> product = new HashMap<>();
        product.put("name", ""); // Empty name
        product.put("price", -10.0); // Negative price
        product.put("quantity", -5); // Negative quantity
        return product;
    }

    // Convert objects to JSON for API testing
    public String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }

    // Convert JSON to object for response testing
    public <T> T fromJson(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JSON to object", e);
        }
    }
}