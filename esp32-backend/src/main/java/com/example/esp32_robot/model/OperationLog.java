package com.example.esp32_robot.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "operation_log")
@Data
public class OperationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username = "anonymous";  // 操作用户

    @Column(nullable = false, length = 100)
    private String operation;     // 操作类型：login, control, view, export

    @Column(length = 1000)
    private String detail;        // 操作详情

    @Column(nullable = false)
    private String ipAddress;     // IP地址

    @Column(nullable = false)
    private LocalDateTime operationTime = LocalDateTime.now();

    @Column(nullable = false)
    private Boolean success = true;  // 操作是否成功
}