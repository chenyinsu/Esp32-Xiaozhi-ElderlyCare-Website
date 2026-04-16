package com.example.esp32_robot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {
    private Long id;
    private String deviceId;
    private Long userId;
    private String content;
    private String senderType; // USER / DEVICE
    private LocalDateTime timestamp;
    private Boolean isRead;
    private String messageType; // TEXT / IMAGE / VOICE

    // 扩展字段：用于前端展示
    private String formattedTime; // 格式化时间
    private String userName;      // 用户名称（可选）
    private String deviceName;    // 设备名称（可选）
}