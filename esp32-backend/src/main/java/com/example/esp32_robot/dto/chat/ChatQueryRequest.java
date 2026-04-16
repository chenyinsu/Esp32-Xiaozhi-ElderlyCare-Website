package com.example.esp32_robot.dto.chat;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatQueryRequest {
    private String deviceId;
    private Long userId;
    private String type;
    private String keyword;
    private Double minSentiment;
    private Double maxSentiment;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 1;
    private Integer size = 20;
}