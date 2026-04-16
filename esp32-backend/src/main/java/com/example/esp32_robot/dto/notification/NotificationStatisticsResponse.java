package com.example.esp32_robot.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatisticsResponse {
    private Long totalNotifications;
    private Long unreadCount;
    private Long readCount;
    private Long urgentCount;
    private Long reminderCount;
    private Long emergencyCount;
    private Long reportCount;
    private Long systemCount;
}