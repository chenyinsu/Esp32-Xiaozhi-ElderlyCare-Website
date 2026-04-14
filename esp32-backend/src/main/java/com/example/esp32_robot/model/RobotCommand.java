package com.example.esp32_robot.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "robot_command")
@Data
public class RobotCommand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String action;        // 指令：forward, backward, left, right, stop, led_on, led_off

    @Column(length = 50)
    private String source;        // 指令来源：web, mobile, api

    @Column(nullable = false)
    private LocalDateTime commandTime = LocalDateTime.now();

    @Column(nullable = false)
    private String status = "pending";  // pending, executing, completed, failed

    @Column(length = 500)
    private String remark;        // 备注信息

    @Column
    private String executedBy;    // 执行设备（ESP32设备ID）

    @Column
    private LocalDateTime executedTime; // 实际执行时间
}