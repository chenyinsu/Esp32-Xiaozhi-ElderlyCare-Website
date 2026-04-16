package com.example.esp32_robot.dto.notification;

import com.example.esp32_robot.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationRequest {

    @NotBlank(message = "通知标题不能为空")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    private String content;

    @NotNull(message = "通知类型不能为空")
    private Notification.NotificationType type = Notification.NotificationType.SYSTEM;

    @NotNull(message = "通知级别不能为空")
    private Notification.NotificationLevel level = Notification.NotificationLevel.INFO;

    private Long senderId;

    @NotNull(message = "接收者ID不能为空")
    private Long receiverId;

    private Long relatedReminderId;

    private Long relatedEmergencyId;

    private Long relatedReportId;

    private LocalDateTime expiresAt;
}