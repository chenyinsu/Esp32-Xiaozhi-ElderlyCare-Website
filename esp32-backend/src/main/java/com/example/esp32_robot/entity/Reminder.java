package com.example.esp32_robot.entity;
// Reminder.java

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String content; // "早上8点 - 服用高血压药"

    @Column(nullable = false)
    private String reminderTime; // "08:00"

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'daily'")
    private String repeat; // "once", "daily", "weekly"

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean enabled;

    private String note; // "饭后30分钟"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}