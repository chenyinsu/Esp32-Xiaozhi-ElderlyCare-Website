package com.example.esp32_robot.dto.emergency;

import com.example.esp32_robot.entity.Emergency;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class EmergencyRequest {

    @NotNull(message = "紧急事件类型不能为空")
    private Emergency.EmergencyType emergencyType = Emergency.EmergencyType.BUTTON_PRESS;

    @NotNull(message = "紧急程度不能为空")
    private Emergency.EmergencyLevel emergencyLevel = Emergency.EmergencyLevel.CRITICAL;

    @NotNull(message = "触发时间不能为空")
    private LocalDateTime triggerTime;

    private String triggerSource;

    private String description;

    private String audioRecordUrl;

    private String videoRecordUrl;

    private String sensorData;

    private Double locationLatitude;

    private Double locationLongitude;

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private List<Long> notifiedContacts;
}
