package com.erp.system.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;

public class StructuredLogger {
    
    private static final Logger SECURITY_LOGGER = LoggerFactory.getLogger("SECURITY");
    private static final Logger AUDIT_LOGGER = LoggerFactory.getLogger("AUDIT");
    private static final Logger APPLICATION_LOGGER = LoggerFactory.getLogger(StructuredLogger.class);

    public static void logSecurityEvent(String event, String userId, String details) {
        try {
            MDC.put("event_type", "security");
            MDC.put("user_id", userId);
            MDC.put("event_id", UUID.randomUUID().toString());
            MDC.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            SECURITY_LOGGER.info("Security Event: {} - User: {} - Details: {}", event, userId, details);
        } finally {
            MDC.clear();
        }
    }

    public static void logAuditEvent(String action, String userId, String resourceId, Map<String, Object> metadata) {
        try {
            MDC.put("event_type", "audit");
            MDC.put("user_id", userId);
            MDC.put("resource_id", resourceId);
            MDC.put("action", action);
            MDC.put("event_id", UUID.randomUUID().toString());
            MDC.put("timestamp", String.valueOf(System.currentTimeMillis()));
            
            if (metadata != null) {
                metadata.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
            }
            
            AUDIT_LOGGER.info("Audit Event: {} - User: {} - Resource: {}", action, userId, resourceId);
        } finally {
            MDC.clear();
        }
    }

    public static void logBusinessEvent(String service, String operation, String correlationId, Map<String, Object> context) {
        try {
            MDC.put("service", service);
            MDC.put("operation", operation);
            MDC.put("correlation_id", correlationId);
            MDC.put("event_type", "business");
            
            if (context != null) {
                context.forEach((key, value) -> MDC.put(key, String.valueOf(value)));
            }
            
            APPLICATION_LOGGER.info("Business Event: {} - Operation: {}", service, operation);
        } finally {
            MDC.clear();
        }
    }

    public static void logPerformanceMetric(String operation, long durationMs, String status) {
        try {
            MDC.put("event_type", "performance");
            MDC.put("operation", operation);
            MDC.put("duration_ms", String.valueOf(durationMs));
            MDC.put("status", status);
            
            APPLICATION_LOGGER.info("Performance: {} took {}ms - Status: {}", operation, durationMs, status);
        } finally {
            MDC.clear();
        }
    }
}