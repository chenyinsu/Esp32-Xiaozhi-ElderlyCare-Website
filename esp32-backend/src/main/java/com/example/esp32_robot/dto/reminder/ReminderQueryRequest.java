package com.example.esp32_robot.dto.reminder;

import com.example.esp32_robot.entity.Reminder;
import lombok.Data;

@Data
public class ReminderQueryRequest {
    private Long deviceId;
    private Long userId;
    private Reminder.ReminderType reminderType;
    private Reminder.RepeatType repeatType;
    private Boolean isActive;
    private Boolean isTaken;
    private String medicationName;
    private Integer page = 1;
    private Integer size = 20;
}