package com.example.esp32_robot.dto.device;

import com.example.esp32_robot.entity.Device;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeviceResponse {
    private Long id;
    private String deviceId;
    private String deviceName;
    private String firmwareVersion;
    private Device.Status status;
    private LocalDateTime lastOnlineTime;
    private Integer batteryLevel;
    private Integer volumeLevel;
    private Boolean hasCamera;
    private String cameraUrl;
    private Device.ButtonMode button1Mode;
    private Device.ButtonMode button2Mode;
    private String button1Function;
    private String button2Function;
    private Long userId;
    private String userName;
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastLocationTime;
    private String wifiSsid;
    private Integer wifiStrength;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long reminderCount;
    private Long emergencyCount;
}