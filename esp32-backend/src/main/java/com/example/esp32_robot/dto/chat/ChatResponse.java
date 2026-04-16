package com.example.esp32_robot.dto.chat;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatResponse {
    private Long id;
    private String deviceId;
    private Long userId;
    private String type;
    private String content;
    private String audioUrl;
    private String mood;
    private Double sentiment;
    private String keywordsJson;
    private LocalDateTime timestamp;
}