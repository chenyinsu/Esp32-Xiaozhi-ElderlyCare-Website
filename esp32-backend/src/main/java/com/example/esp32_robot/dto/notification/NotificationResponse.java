package com.example.esp32_robot.dto.notification;

import com.example.esp32_robot.entity.Notification;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private Notification.NotificationType type;
    private Notification.NotificationLevel level;
    private Long senderId;
    private String senderName;
    private Long receiverId;
    private String receiverName;
    private Boolean isRead;
    private LocalDateTime readTime;
    private Long relatedReminderId;
    private Long relatedEmergencyId;
    private Long relatedReportId;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean isExpired;
}