package com.erp.system.notification;

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
public class NotificationRequest {
    
    private NotificationType type;
    private String title;
    private String message;
    private String userId;
    private String userRole;
    private String department;
    private NotificationPriority priority;
    private Map<String, Object> data;
    private List<NotificationChannel> channels;
    private LocalDateTime expiresAt;
    private boolean actionRequired;
    private String actionUrl;
    private String sourceService;
    private String entityType;
    private String entityId;
}