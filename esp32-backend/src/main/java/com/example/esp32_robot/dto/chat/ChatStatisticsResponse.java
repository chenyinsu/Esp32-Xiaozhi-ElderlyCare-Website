package com.example.esp32_robot.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatStatisticsResponse {
    private Long totalMessages;
    private Long textMessages;
    private Long audioMessages;
    private Double averageSentiment;
    private String dominantMood;
}