package com.example.esp32_robot.dto.reminder;

import com.example.esp32_robot.entity.Reminder;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalTime;
import java.util.List;

@Data
public class ReminderRequest {

    @NotBlank(message = "提醒标题不能为空")
    private String title;

    private String content;

    private String voiceContent;

    @NotNull(message = "提醒类型不能为空")
    private Reminder.ReminderType reminderType = Reminder.ReminderType.MEDICATION;

    @NotNull(message = "重复类型不能为空")
    private Reminder.RepeatType repeatType = Reminder.RepeatType.NONE;

    private List<Reminder.DayOfWeek> repeatDays;

    @NotNull(message = "提醒时间不能为空")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime remindTime;

    private String startDate;

    private String endDate;

    private Boolean isActive = true;

    private String doseAmount;

    private String medicationName;

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private Long createdBy;
}