package com.example.esp32_robot.dto.emergency;

import com.example.esp32_robot.entity.Emergency;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EmergencyResponse {
    private Long id;
    private Emergency.EmergencyType emergencyType;
    private Emergency.EmergencyLevel emergencyLevel;
    private Emergency.Status status;
    private LocalDateTime triggerTime;
    private String triggerSource;
    private String description;
    private String audioRecordUrl;
    private String videoRecordUrl;
    private String sensorData;
    private Double locationLatitude;
    private Double locationLongitude;
    private Long deviceId;
    private String deviceName;
    private Long userId;
    private String userName;
    private Long handledById;
    private String handledByName;
    private List<Long> notifiedContacts;
    private LocalDateTime firstContactTime;
    private LocalDateTime firstResponseTime;
    private LocalDateTime resolvedTime;
    private LocalDateTime closedTime;
    private String resolutionNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long recordCount;
}
