package com.example.esp32_robot.dto.emergencyrecord;

import com.example.esp32_robot.entity.EmergencyRecord;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmergencyRecordQueryRequest {
    private Long emergencyId;
    private Long userId;
    private EmergencyRecord.ActionType actionType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 1;
    private Integer size = 20;
}