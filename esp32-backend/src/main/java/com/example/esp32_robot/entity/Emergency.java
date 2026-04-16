package com.example.esp32_robot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 紧急事件实体类
 * 记录按钮触发、系统检测等紧急事件
 */
@Entity
@Table(name = "emergency")
@Data
@ToString(exclude = {"device", "user", "handledBy"})
@EqualsAndHashCode(exclude = {"device", "user", "handledBy"})
public class Emergency {

    public enum EmergencyType {
        BUTTON_PRESS,           // 按钮触发
        FALL_DETECTION,         // 跌倒检测
        HEALTH_ABNORMAL,        // 健康异常
        NO_MOVEMENT,            // 长时间无活动
        OTHER                   // 其他
    }

    public enum EmergencyLevel {
        LOW,        // 低风险
        MEDIUM,     // 中风险
        HIGH,       // 高风险
        CRITICAL    // 紧急
    }

    public enum Status {
        PENDING,    // 待处理
        HANDLING,   // 处理中
        RESOLVED,   // 已解决
        CLOSED,     // 已关闭
        FALSE_ALARM // 误报
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyType emergencyType = EmergencyType.BUTTON_PRESS;  // 紧急事件类型

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmergencyLevel emergencyLevel = EmergencyLevel.CRITICAL;  // 紧急程度

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;  // 处理状态

    @Column(name = "trigger_time", nullable = false)
    private LocalDateTime triggerTime;  // 触发时间

    @Column(name = "trigger_source")
    private String triggerSource;  // 触发源（如"button1"、"button2"、"sensor"等）

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;    // 事件描述

    @Column(name = "audio_record_url")
    private String audioRecordUrl; // 录音文件地址（如有）

    @Column(name = "video_record_url")
    private String videoRecordUrl; // 视频记录地址（如有）

    @Column(name = "sensor_data", columnDefinition = "TEXT")
    private String sensorData;     // 传感器数据（JSON格式）

    @Column(name = "location_latitude")
    private Double locationLatitude;  // 事件发生地点纬度

    @Column(name = "location_longitude")
    private Double locationLongitude; // 事件发生地点经度

    // 关联设备
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    // 关联用户（老年人）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 处理人（社区工作人员）
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy;

    // 处理记录
    @OneToMany(mappedBy = "emergency", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EmergencyRecord> records = new ArrayList<>();

    // 通知的紧急联系人
    @ElementCollection
    @CollectionTable(name = "emergency_notified_contacts", joinColumns = @JoinColumn(name = "emergency_id"))
    @Column(name = "contact_id")
    private List<Long> notifiedContacts = new ArrayList<>();

    @Column(name = "first_contact_time")
    private LocalDateTime firstContactTime;  // 首次联系时间

    @Column(name = "first_response_time")
    private LocalDateTime firstResponseTime; // 首次响应时间

    @Column(name = "resolved_time")
    private LocalDateTime resolvedTime;      // 解决时间

    @Column(name = "closed_time")
    private LocalDateTime closedTime;        // 关闭时间

    @Column(name = "resolution_note", columnDefinition = "TEXT")
    private String resolutionNote;           // 解决说明

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}