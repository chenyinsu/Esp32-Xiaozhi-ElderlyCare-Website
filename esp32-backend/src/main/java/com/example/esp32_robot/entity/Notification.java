package com.example.esp32_robot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 消息通知实体类
 * 用于系统内部消息通知
 */
@Entity
@Table(name = "notification")
@Data
@ToString(exclude = {"sender", "receiver"})
@EqualsAndHashCode(exclude = {"sender", "receiver"})
public class Notification {

    public enum NotificationType {
        REMINDER,           // 提醒通知
        EMERGENCY,          // 紧急事件通知
        REPORT,             // 报告通知
        SYSTEM,             // 系统通知
        MESSAGE             // 普通消息
    }

    public enum NotificationLevel {
        INFO,       // 信息
        WARNING,    // 警告
        DANGER,     // 危险
        CRITICAL    // 紧急
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;           // 通知标题

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;         // 通知内容

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type = NotificationType.SYSTEM;  // 通知类型

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationLevel level = NotificationLevel.INFO;  // 通知级别

    // 发送者
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    // 接收者
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "is_read")
    private Boolean isRead = false;  // 是否已读

    @Column(name = "read_time")
    private LocalDateTime readTime;  // 阅读时间

    // 关联的业务ID
    @Column(name = "related_reminder_id")
    private Long relatedReminderId;  // 关联的提醒ID

    @Column(name = "related_emergency_id")
    private Long relatedEmergencyId; // 关联的紧急事件ID

    @Column(name = "related_report_id")
    private Long relatedReportId;    // 关联的报告ID

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;  // 过期时间

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}