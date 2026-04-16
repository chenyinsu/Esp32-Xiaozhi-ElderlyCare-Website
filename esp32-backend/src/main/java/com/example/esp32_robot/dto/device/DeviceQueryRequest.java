package com.example.esp32_robot.dto.device;

import com.example.esp32_robot.entity.Device;
import lombok.Data;

@Data
public class DeviceQueryRequest {
    private String deviceId;
    private String deviceName;
    private Device.Status status;
    private Boolean hasCamera;
    private Boolean boundToUser;
    private Integer minBattery;
    private Integer maxBattery;
    private Double minLatitude;
    private Double maxLatitude;
    private Double minLongitude;
    private Double maxLongitude;
    private Integer page = 1;
    private Integer size = 20;
}