package com.example.esp32_robot.entity;

// Emergency.java
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emergency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private String eventType; // "button_pressed", "fall_detected"

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'critical'")
    private String level; // "critical", "warning", "info"

    @Column(columnDefinition = "VARCHAR(20) DEFAULT 'pending'")
    private String status; // "pending", "acknowledged", "resolved"

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean notified;

    private String note;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;
}