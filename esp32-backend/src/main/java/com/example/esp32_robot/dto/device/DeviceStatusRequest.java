package com.example.esp32_robot.dto.device;

import com.example.esp32_robot.entity.Device;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceStatusRequest {
    @NotNull(message = "设备状态不能为空")
    private Device.Status status;

    private Integer batteryLevel;

    private Double latitude;

    private Double longitude;

    private Integer wifiStrength;
}