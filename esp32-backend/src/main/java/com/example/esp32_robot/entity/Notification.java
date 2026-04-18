package com.example.esp32_robot.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@Builder  // ✅ 添加这个注解
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"sender", "receiver"})
@EqualsAndHashCode(exclude = {"sender", "receiver"})
public class Notification {

    public enum NotificationType {
        REMINDER,
        EMERGENCY,
        REPORT,
        SYSTEM,
        MESSAGE
    }

    public enum NotificationLevel {
        INFO,
        WARNING,
        DANGER,
        CRITICAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationType type = NotificationType.SYSTEM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationLevel level = NotificationLevel.INFO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "read_time")
    private LocalDateTime readTime;

    @Column(name = "related_reminder_id")
    private Long relatedReminderId;

    @Column(name = "related_emergency_id")
    private Long relatedEmergencyId;

    @Column(name = "related_report_id")
    private Long relatedReportId;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
}