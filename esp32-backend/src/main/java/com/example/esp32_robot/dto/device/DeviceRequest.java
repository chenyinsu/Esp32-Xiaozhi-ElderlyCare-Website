package com.example.esp32_robot.dto.device;

import com.example.esp32_robot.entity.Device;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceRequest {

    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @NotBlank(message = "设备名称不能为空")
    private String deviceName;

    private String firmwareVersion;

    private Device.Status status = Device.Status.OFFLINE;

    private Integer batteryLevel;

    private Integer volumeLevel = 50;

    private Boolean hasCamera = false;

    private String cameraUrl;

    private String cameraUsername;

    private String cameraPassword;

    private Device.ButtonMode button1Mode = Device.ButtonMode.EMERGENCY;

    private Device.ButtonMode button2Mode = Device.ButtonMode.SINGLE_CLICK;

    private String button1Function = "紧急求助";

    private String button2Function = "状态确认";

    private Double latitude;

    private Double longitude;

    private String wifiSsid;

    private Integer wifiStrength;

    private Long userId;
}