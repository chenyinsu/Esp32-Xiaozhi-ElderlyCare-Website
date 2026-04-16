package com.example.esp32_robot.dto.report;

import com.example.esp32_robot.entity.Report;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ReportQueryRequest {
    private Long deviceId;
    private Long userId;
    private Report.ReportType reportType;
    private Report.HealthStatus healthStatus;
    private Boolean isRead;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer page = 1;
    private Integer size = 20;
}