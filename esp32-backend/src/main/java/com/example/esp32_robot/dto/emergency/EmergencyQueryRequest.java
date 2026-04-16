package com.example.esp32_robot.dto.emergency;

import com.example.esp32_robot.entity.Emergency;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmergencyQueryRequest {
    private Emergency.Status status;
    private Emergency.EmergencyLevel emergencyLevel;
    private Emergency.EmergencyType emergencyType;
    private Long deviceId;
    private Long userId;
    private Long handledById;
    private String triggerSource;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer page = 1;
    private Integer size = 20;
}