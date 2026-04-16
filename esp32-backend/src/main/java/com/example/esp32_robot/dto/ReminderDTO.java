package com.example.esp32_robot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReminderDTO {
    private Long id;
    private String title;
    private String description;
    private String reminderer; // 提醒人
    private String deviceId;
    private String frequency; // ONCE / DAILY / WEEKLY / MONTHLY
    private LocalTime remindTime;
    private LocalDateTime nextReminderTime;
    private Boolean isRecurring;
    private String priority; // LOW / MEDIUM / HIGH
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private String note;
    private LocalDateTime createdAt;
}