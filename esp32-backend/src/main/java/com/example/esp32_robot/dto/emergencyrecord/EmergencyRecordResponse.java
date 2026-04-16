package com.example.esp32_robot.dto.emergencyrecord;

import com.example.esp32_robot.entity.EmergencyRecord;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmergencyRecordResponse {
    private Long id;
    private EmergencyRecord.ActionType actionType;
    private String content;
    private Long emergencyId;
    private Long userId;
    private String userName;
    private LocalDateTime actionTime;
    private String attachmentUrl;
    private LocalDateTime createdAt;
}