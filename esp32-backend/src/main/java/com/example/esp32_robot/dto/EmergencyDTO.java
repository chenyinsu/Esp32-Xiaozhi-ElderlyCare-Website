package com.example.esp32_robot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyDTO {
    private Long id;
    private String deviceId;
    private String eventType;
    private String status; // PENDING / HANDLING / RESOLVED / CLOSED / FALSE_ALARM
    private String level;  // LOW / MEDIUM / HIGH / CRITICAL
    private String severityScore; // 严重度评分
    private LocalDateTime timestamp;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private Long handlerId;
    private String handlerName; // 处理人姓名
    private String contactPerson;
    private String contactPhone;
    private String location;
    private String note;
    private String source; // 报警来源（传感器 / 手动 / 系统）
    private Boolean notified;

    // 扩展字段
    private String formattedTime;
    private String levelColor;
    private String statusText;
    private Long responseTimeMinutes; // 响应时间（分钟）
}