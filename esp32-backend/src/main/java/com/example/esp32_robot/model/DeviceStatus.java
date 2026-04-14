package com.example.esp32_robot.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_status")
@Data
public class DeviceStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String deviceId = "ESP32_001";  // 设备唯一标识

    @Column
    private Double batteryLevel;  // 电池电量百分比

    @Column
    private Double temperature;   // 温度

    @Column
    private Double humidity;      // 湿度

    @Column
    private String wifiStrength;  // WiFi信号强度

    @Column(nullable = false)
    private Boolean online = true;  // 是否在线

    @Column(nullable = false)
    private LocalDateTime reportTime = LocalDateTime.now();  // 上报时间

    @Column(length = 1000)
    private String extraInfo;     // 额外信息（JSON格式存储）
}