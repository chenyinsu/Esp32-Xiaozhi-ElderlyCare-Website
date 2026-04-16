package com.example.esp32_robot.service;

import com.example.esp32_robot.dto.report.*;
import com.example.esp32_robot.dto.common.PageResponse;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    ReportResponse createReport(ReportRequest request);

    ReportResponse updateReport(Long id, ReportRequest request);

    ReportResponse getReportById(Long id);

    PageResponse<ReportResponse> queryReports(ReportQueryRequest request);

    List<ReportResponse> getReportsByDeviceId(Long deviceId);

    List<ReportResponse> getReportsByUserId(Long userId);

    HealthTrendResponse getHealthTrend(Long userId, LocalDate startDate);

    ReportResponse generateDailyHealthReport(Long userId, Long deviceId);

    void markReportAsRead(Long id, Long readBy);

    void deleteReport(Long id);
}
