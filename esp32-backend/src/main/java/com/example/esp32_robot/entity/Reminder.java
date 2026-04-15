package com.example.esp32_robot.entity;
// Reminder.java

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reminders")
@Data
public class Reminder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String deviceId = "esp32-01";

    @Column(length = 500)
    private String content;

    private LocalTime reminderTime;

    @Column(length = 20)
    private String repeat = "daily"; // daily, weekly, once

    private Boolean enabled = true;

    @Column(length = 1000)
    private String note;

    private LocalDateTime nextTriggerTime;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}