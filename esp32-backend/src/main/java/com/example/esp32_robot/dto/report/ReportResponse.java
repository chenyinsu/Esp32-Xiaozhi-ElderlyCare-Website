package com.example.esp32_robot.dto.report;

import com.example.esp32_robot.entity.Report;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportResponse {
    private Long id;
    private Report.ReportType reportType;
    private String title;
    private String content;
    private Report.HealthStatus healthStatus;
    private Integer heartRate;
    private Integer bloodPressureSys;
    private Integer bloodPressureDia;
    private Double bodyTemperature;
    private Integer bloodOxygen;
    private Integer stepCount;
    private Double sleepDuration;
    private Integer medicationTaken;
    private Integer medicationMissed;
    private Integer fallDetected;
    private Integer emergencyCalls;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate reportDate;
    private Long deviceId;
    private String deviceName;
    private Long userId;
    private String userName;
    private String healthAdvice;
    private String medicationAdvice;
    private String activityAdvice;
    private List<Long> reminderRefs;
    private List<Long> emergencyRefs;
    private Long generatedBy;
    private String generatedByName;
    private Boolean isRead;
    private Long readBy;
    private LocalDateTime readTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}