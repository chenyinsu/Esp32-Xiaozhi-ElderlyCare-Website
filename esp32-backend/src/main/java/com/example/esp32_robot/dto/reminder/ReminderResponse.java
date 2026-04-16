package com.example.esp32_robot.dto.reminder;

import com.example.esp32_robot.entity.Reminder;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class ReminderResponse {
    private Long id;
    private String title;
    private String content;
    private String voiceContent;
    private Reminder.ReminderType reminderType;
    private Reminder.RepeatType repeatType;
    private List<Reminder.DayOfWeek> repeatDays;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime remindTime;
    private String startDate;
    private String endDate;
    private Boolean isActive;
    private LocalDateTime lastTriggered;
    private LocalDateTime nextTrigger;
    private String doseAmount;
    private String medicationName;
    private Boolean isTaken;
    private LocalDateTime takenTime;
    private Long deviceId;
    private String deviceName;
    private Long userId;
    private String userName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdBy;
    private String createdByName;
}
