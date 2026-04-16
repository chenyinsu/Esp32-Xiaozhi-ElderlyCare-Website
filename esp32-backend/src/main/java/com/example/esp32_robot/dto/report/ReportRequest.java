package com.example.esp32_robot.dto.report;

import com.example.esp32_robot.entity.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class ReportRequest {

    @NotNull(message = "报告类型不能为空")
    private Report.ReportType reportType = Report.ReportType.DAILY_HEALTH;

    @NotBlank(message = "报告标题不能为空")
    private String title;

    @NotBlank(message = "报告内容不能为空")
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

    private Integer fallDetected = 0;

    private Integer emergencyCalls = 0;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private LocalDate reportDate = LocalDate.now();

    @NotNull(message = "设备ID不能为空")
    private Long deviceId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String healthAdvice;

    private String medicationAdvice;

    private String activityAdvice;

    private List<Long> reminderRefs;

    private List<Long> emergencyRefs;

    private Long generatedBy;
}