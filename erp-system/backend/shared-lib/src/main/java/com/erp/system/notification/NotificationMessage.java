package com.erp.system.notification;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    
    private String id;
    private NotificationType type;
    private String title;
    private String message;
    private String userId;
    private String userRole;
    private String department;
    private NotificationPriority priority;
    private Map<String, Object> data;
    private List<NotificationChannel> channels;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expiresAt;
    
    private boolean actionRequired;
    private String actionUrl;
    private String sourceService;
    private String entityType;
    private String entityId;
    private boolean read;
    private boolean delivered;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deliveredAt;
}

enum NotificationType {
    SYSTEM_ALERT,
    BUSINESS_EVENT,
    APPROVAL_REQUEST,
    TASK_REMINDER,
    PERFORMANCE_ALERT,
    SECURITY_ALERT,
    USER_MESSAGE,
    WORKFLOW_UPDATE,
    DEADLINE_APPROACHING,
    INVENTORY_ALERT,
    FINANCIAL_ALERT,
    HR_NOTIFICATION,
    PRODUCTION_UPDATE,
    SALES_NOTIFICATION,
    PURCHASE_UPDATE
}

enum NotificationPriority {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    URGENT(4),
    CRITICAL(5);
    
    private final int level;
    
    NotificationPriority(int level) {
        this.level = level;
    }
    
    public int getLevel() {
        return level;
    }
}

enum NotificationChannel {
    EMAIL,
    SMS,
    WEBSOCKET,
    IN_APP,
    SLACK,
    TEAMS,
    PUSH_NOTIFICATION
}

enum AlertSeverity {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}