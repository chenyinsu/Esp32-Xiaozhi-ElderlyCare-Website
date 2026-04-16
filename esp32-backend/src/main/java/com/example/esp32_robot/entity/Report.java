package com.example.esp32_robot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 健康报告实体类
 * 记录老年人的健康数据和活动情况
 */
@Entity
@Table(name = "report")
@Data
@ToString(exclude = {"device", "user"})
@EqualsAndHashCode(exclude = {"device", "user"})
public class Report {

    public enum ReportType {
        DAILY_HEALTH,       // 日常健康报告
        WEEKLY_SUMMARY,     // 周度总结
        MONTHLY_SUMMARY,    // 月度总结
        MEDICATION_LOG,     // 用药记录
        ACTIVITY_LOG,       // 活动记录
        EMERGENCY_REPORT    // 紧急事件报告
    }

    public enum HealthStatus {
        EXCELLENT,  // 优秀
        GOOD,       // 良好
        FAIR,       // 一般
        POOR,       // 差
        CRITICAL    // 危急
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportType reportType = ReportType.DAILY_HEALTH;  // 报告类型

    @Column(nullable = false)
    private String title;           // 报告标题

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;         // 报告详细内容

    @Enumerated(EnumType.STRING)
    private HealthStatus healthStatus;  // 健康状况评级

    // 健康数据指标
    @Column(name = "heart_rate")
    private Integer heartRate;      // 心率（bpm）

    @Column(name = "blood_pressure_sys")
    private Integer bloodPressureSys;  // 收缩压

    @Column(name = "blood_pressure_dia")
    private Integer bloodPressureDia;  // 舒张压

    @Column(name = "body_temperature")
    private Double bodyTemperature;  // 体温（℃）

    @Column(name = "blood_oxygen")
    private Integer bloodOxygen;     // 血氧饱和度（%）

    @Column(name = "step_count")
    private Integer stepCount;       // 步数

    @Column(name = "sleep_duration")
    private Double sleepDuration;    // 睡眠时长（小时）

    @Column(name = "medication_taken")
    private Integer medicationTaken;  // 按时服药次数

    @Column(name = "medication_missed")
    private Integer medicationMissed; // 未服药次数

    // 设备传感器数据
    @Column(name = "fall_detected")
    private Integer fallDetected = 0;  // 跌倒检测次数

    @Column(name = "emergency_calls")
    private Integer emergencyCalls = 0;  // 紧急呼叫次数

    // 时间范围
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;     // 报告开始日期

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;       // 报告结束日期

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate = LocalDate.now();  // 报告生成日期

    // 关联设备
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    // 关联用户（老年人）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 生成的提醒和建议
    @Column(name = "health_advice", columnDefinition = "TEXT")
    private String healthAdvice;     // 健康建议

    @Column(name = "medication_advice", columnDefinition = "TEXT")
    private String medicationAdvice;  // 用药建议

    @Column(name = "activity_advice", columnDefinition = "TEXT")
    private String activityAdvice;    // 活动建议

    // 关联的提醒记录
    @ElementCollection
    @CollectionTable(name = "report_reminder_refs", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "reminder_id")
    private List<Long> reminderRefs = new ArrayList<>();

    // 关联的紧急事件
    @ElementCollection
    @CollectionTable(name = "report_emergency_refs", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "emergency_id")
    private List<Long> emergencyRefs = new ArrayList<>();

    @Column(name = "generated_by")
    private Long generatedBy;        // 报告生成者（系统或社区工作人员）

    @Column(name = "is_read")
    private Boolean isRead = false;  // 是否已读

    @Column(name = "read_by")
    private Long readBy;             // 阅读者

    @Column(name = "read_time")
    private LocalDateTime readTime;  // 阅读时间

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}