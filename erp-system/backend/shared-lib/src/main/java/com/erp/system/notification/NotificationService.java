package com.erp.system.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String NOTIFICATION_TOPIC = "notifications";
    
    public void sendNotification(NotificationRequest request) {
        try {
            NotificationMessage message = NotificationMessage.builder()
                    .id(UUID.randomUUID().toString())
                    .type(request.getType())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .userId(request.getUserId())
                    .userRole(request.getUserRole())
                    .department(request.getDepartment())
                    .priority(request.getPriority())
                    .data(request.getData())
                    .channels(request.getChannels())
                    .timestamp(LocalDateTime.now())
                    .expiresAt(request.getExpiresAt())
                    .actionRequired(request.isActionRequired())
                    .actionUrl(request.getActionUrl())
                    .build();
            
            kafkaTemplate.send(NOTIFICATION_TOPIC, message.getId(), message);
            log.info("Notification sent: {} to user: {}", message.getType(), message.getUserId());
            
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }
    
    public void sendSystemAlert(String title, String message, AlertSeverity severity) {
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.SYSTEM_ALERT)
                .title(title)
                .message(message)
                .priority(mapSeverityToPriority(severity))
                .channels(java.util.List.of(NotificationChannel.EMAIL, NotificationChannel.WEBSOCKET))
                .actionRequired(severity == AlertSeverity.CRITICAL)
                .build();
        
        sendNotification(request);
    }
    
    public void sendBusinessNotification(String service, String entity, String action, 
                                       Long entityId, String userId, Map<String, Object> data) {
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.BUSINESS_EVENT)
                .title(String.format("%s %s", entity, action))
                .message(String.format("%s with ID %d has been %s", entity, entityId, action))
                .userId(userId)
                .priority(NotificationPriority.MEDIUM)
                .channels(java.util.List.of(NotificationChannel.WEBSOCKET, NotificationChannel.IN_APP))
                .data(data)
                .build();
        
        sendNotification(request);
    }
    
    public void sendApprovalRequest(String title, String message, String userId, 
                                  String actionUrl, Map<String, Object> data) {
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.APPROVAL_REQUEST)
                .title(title)
                .message(message)
                .userId(userId)
                .priority(NotificationPriority.HIGH)
                .channels(java.util.List.of(NotificationChannel.EMAIL, NotificationChannel.WEBSOCKET, NotificationChannel.IN_APP))
                .actionRequired(true)
                .actionUrl(actionUrl)
                .data(data)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        
        sendNotification(request);
    }
    
    public void sendTaskReminder(String userId, String taskTitle, String taskUrl, LocalDateTime dueDate) {
        Map<String, Object> data = new HashMap<>();
        data.put("taskTitle", taskTitle);
        data.put("dueDate", dueDate);
        data.put("taskUrl", taskUrl);
        
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.TASK_REMINDER)
                .title("Task Reminder")
                .message(String.format("Task '%s' is due on %s", taskTitle, dueDate))
                .userId(userId)
                .priority(NotificationPriority.MEDIUM)
                .channels(java.util.List.of(NotificationChannel.IN_APP, NotificationChannel.EMAIL))
                .actionRequired(true)
                .actionUrl(taskUrl)
                .data(data)
                .build();
        
        sendNotification(request);
    }
    
    public void sendPerformanceAlert(String metric, Double value, Double threshold, 
                                   String service, AlertSeverity severity) {
        Map<String, Object> data = new HashMap<>();
        data.put("metric", metric);
        data.put("value", value);
        data.put("threshold", threshold);
        data.put("service", service);
        
        NotificationRequest request = NotificationRequest.builder()
                .type(NotificationType.PERFORMANCE_ALERT)
                .title(String.format("Performance Alert: %s", metric))
                .message(String.format("%s value %.2f exceeds threshold %.2f in %s", 
                        metric, value, threshold, service))
                .priority(mapSeverityToPriority(severity))
                .channels(java.util.List.of(NotificationChannel.EMAIL, NotificationChannel.WEBSOCKET, NotificationChannel.SMS))
                .actionRequired(severity == AlertSeverity.CRITICAL)
                .data(data)
                .build();
        
        sendNotification(request);
    }
    
    private NotificationPriority mapSeverityToPriority(AlertSeverity severity) {
        return switch (severity) {
            case LOW -> NotificationPriority.LOW;
            case MEDIUM -> NotificationPriority.MEDIUM;
            case HIGH -> NotificationPriority.HIGH;
            case CRITICAL -> NotificationPriority.URGENT;
        };
    }
}