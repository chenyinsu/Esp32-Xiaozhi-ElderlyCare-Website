package com.example.esp32_robot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 提醒实体类
 * 支持用药提醒、闹钟提醒、活动提醒等
 */
@Entity
@Table(name = "reminder")
@Data
@ToString(exclude = {"device", "user"})
@EqualsAndHashCode(exclude = {"device", "user"})
public class Reminder {

    public enum ReminderType {
        MEDICATION,     // 用药提醒
        ALARM,          // 闹钟提醒
        ACTIVITY,       // 活动提醒
        APPOINTMENT,    // 预约提醒
        CUSTOM          // 自定义提醒
    }

    public enum RepeatType {
        NONE,           // 不重复
        DAILY,          // 每天
        WEEKLY,         // 每周
        MONTHLY,        // 每月
        WORKDAYS,       // 工作日
        WEEKENDS,       // 周末
        CUSTOM          // 自定义重复
    }

    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY, EVERYDAY
    }

    public enum Priority {
        LOW,        // 低
        MEDIUM,     // 中
        HIGH,       // 高
        CRITICAL    // 紧急
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;           // 提醒标题（如"服用降压药"）

    @Column(columnDefinition = "TEXT")
    private String content;         // 详细内容（如"请服用一片降压药，饭后服用"）

    @Column(name = "voice_content")
    private String voiceContent;    // 语音播报内容（如"该吃降压药了"）

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType reminderType = ReminderType.MEDICATION;  // 提醒类型

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepeatType repeatType = RepeatType.NONE;  // 重复类型

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.MEDIUM;  // 优先级，默认中等


    // 重复的天数（每周的哪几天）
    @ElementCollection
    @CollectionTable(name = "reminder_repeat_days", joinColumns = @JoinColumn(name = "reminder_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> repeatDays = new ArrayList<>();

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime remindTime;   // 提醒时间（每天的具体时间）

    @Column(name = "start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String startDate;       // 开始日期

    @Column(name = "end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private String endDate;         // 结束日期

    @Column(name = "is_active")
    private Boolean isActive = true;  // 是否激活

    @Column(name = "last_triggered")
    private LocalDateTime lastTriggered;  // 上次触发时间

    @Column(name = "next_trigger")
    private LocalDateTime nextTrigger;    // 下次触发时间

    @Column(name = "dose_amount")
    private String doseAmount;      // 剂量（如"1片"、"5ml"）

    @Column(name = "medication_name")
    private String medicationName;  // 药品名称

    @Column(name = "is_taken")
    private Boolean isTaken = false;  // 是否已服用（仅用于用药提醒）

    @Column(name = "taken_time")
    private LocalDateTime takenTime;  // 服用时间

    // 关联设备
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    // 关联用户（老年人）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Column(name = "created_by")
    private Long createdBy;          // 创建者（社区工作人员ID）

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}