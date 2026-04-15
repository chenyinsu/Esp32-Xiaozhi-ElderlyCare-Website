package com.example.esp32_robot.entity;

// Chat.java
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "chats")
@Data
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String deviceId = "esp32-01";
    private Long userId = 1001L;

    @Column(length = 20)
    private String type = "text"; // text, audio

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String audioUrl;

    @Column(length = 50)
    private String mood = "neutral";
    private Double sentiment = 0.5;

    @Column(columnDefinition = "TEXT")
    private String keywordsJson;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}