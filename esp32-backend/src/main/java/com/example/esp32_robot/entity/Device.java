package com.example.esp32_robot.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ESP32-S3设备实体类
 */
@Entity
@Table(name = "device")
@Data
@ToString(exclude = {"user", "reminders", "emergencies"})
@EqualsAndHashCode(exclude = {"user", "reminders", "emergencies"})
public class Device {

    public enum Status {
        ONLINE,         // 在线
        OFFLINE,        // 离线
        MAINTENANCE,    // 维护中
        ERROR           // 错误
    }

    public enum ButtonMode {
        SINGLE_CLICK,   // 单击
        DOUBLE_CLICK,   // 双击
        LONG_PRESS,     // 长按
        EMERGENCY       // 紧急按钮
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String deviceId;        // 设备唯一标识（MAC地址或序列号）

    @Column(nullable = false)
    private String deviceName;      // 设备名称

    @Column(name = "firmware_version")
    private String firmwareVersion; // 固件版本

    @Enumerated(EnumType.STRING)
    private Status status = Status.OFFLINE;  // 设备状态

    @Column(name = "last_online_time")
    private LocalDateTime lastOnlineTime;    // 最后在线时间

    @Column(name = "battery_level")
    private Integer batteryLevel;   // 电池电量（0-100）

    @Column(name = "volume_level")
    private Integer volumeLevel = 50;  // 音量等级

    @Column(name = "has_camera")
    private Boolean hasCamera = false;  // 是否有摄像头

    @Column(name = "camera_url")
    private String cameraUrl;       // 摄像头流地址（RTSP/HTTP）

    @Column(name = "camera_username")
    private String cameraUsername;  // 摄像头用户名

    @Column(name = "camera_password")
    private String cameraPassword;  // 摄像头密码

    @Column(name = "button1_mode")
    @Enumerated(EnumType.STRING)
    private ButtonMode button1Mode = ButtonMode.EMERGENCY;  // 按钮1模式

    @Column(name = "button2_mode")
    @Enumerated(EnumType.STRING)
    private ButtonMode button2Mode = ButtonMode.SINGLE_CLICK;  // 按钮2模式

    @Column(name = "button1_function")
    private String button1Function = "紧急求助";  // 按钮1功能描述

    @Column(name = "button2_function")
    private String button2Function = "状态确认";  // 按钮2功能描述

    // 绑定的用户（老年人）
    @OneToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    // 设备的提醒列表
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Reminder> reminders = new ArrayList<>();

    // 设备的紧急事件列表
    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Emergency> emergencies = new ArrayList<>();

    // 设备的位置信息
    @Column(name = "latitude")
    private Double latitude;        // 纬度

    @Column(name = "longitude")
    private Double longitude;       // 经度

    @Column(name = "last_location_time")
    private LocalDateTime lastLocationTime;  // 最后定位时间

    @Column(name = "wifi_ssid")
    private String wifiSsid;        // 连接WiFi名称

    @Column(name = "wifi_strength")
    private Integer wifiStrength;   // WiFi信号强度

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}