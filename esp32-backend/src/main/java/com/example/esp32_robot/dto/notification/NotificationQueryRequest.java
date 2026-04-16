package com.example.esp32_robot.dto.notification;

import com.example.esp32_robot.entity.Notification;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationQueryRequest {
    private Long receiverId;
    private Long senderId;
    private Boolean isRead;
    private Notification.NotificationType type;
    private Notification.NotificationLevel level;
    private Long relatedEmergencyId;
    private Long relatedReminderId;
    private Long relatedReportId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean includeExpired = false;
    private Integer page = 1;
    private Integer size = 20;
}
