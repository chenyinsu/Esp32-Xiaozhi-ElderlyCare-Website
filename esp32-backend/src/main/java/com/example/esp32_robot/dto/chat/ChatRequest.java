package com.example.esp32_robot.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequest {

    private String deviceId = "esp32-01";

    private Long userId = 1001L;

    @NotBlank(message = "消息类型不能为空")
    private String type = "text";

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private String audioUrl;

    private String mood = "neutral";

    private Double sentiment = 0.5;

    private String keywordsJson;
}