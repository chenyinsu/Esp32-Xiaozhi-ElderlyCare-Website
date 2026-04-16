package com.example.esp32_robot.service.impl;

import com.example.esp32_robot.dto.report.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.converter.DtoConverter;
import com.example.esp32_robot.entity.*;
import com.example.esp32_robot.exception.BusinessException;
import com.example.esp32_robot.exception.ResourceNotFoundException;
import com.example.esp32_robot.repository.*;
import com.example.esp32_robot.service.ReportService;
import com.example.esp32_robot.service.base.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReportServiceImpl extends BaseService implements ReportService {

    private final ReportRepository reportRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;
    private final EmergencyRepository emergencyRepository;
    private final ChatRepository chatRepository;
    private final DtoConverter dtoConverter;

    @Override
    public ReportResponse createReport(ReportRequest request) {
        log.info("Creating report for user: {}", request.getUserId());

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", request.getDeviceId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Report report = dtoConverter.toReportEntity(request);
        report.setDevice(device);
        report.setUser(user);

        // 评估健康状况
        evaluateHealthStatus(report);

        // 生成建议
        generateAdvice(report);

        Report savedReport = reportRepository.save(report);
        log.info("Report created successfully with id: {}", savedReport.getId());

        return dtoConverter.toReportResponse(savedReport);
    }

    private void evaluateHealthStatus(Report report) {
        int score = 100;
        List<String> concerns = new ArrayList<>();

        // 评估心率
        if (report.getHeartRate() != null) {
            if (report.getHeartRate() < 60 || report.getHeartRate() > 100) {
                score -= 20;
                concerns.add("心率异常");
            }
        }

        // 评估血压
        if (report.getBloodPressureSys() != null && report.getBloodPressureDia() != null) {
            if (report.getBloodPressureSys() > 140 || report.getBloodPressureDia() > 90) {
                score -= 20;
                concerns.add("血压偏高");
            } else if (report.getBloodPressureSys() < 90 || report.getBloodPressureDia() < 60) {
                score -= 20;
                concerns.add("血压偏低");
            }
        }

        // 评估血氧
        if (report.getBloodOxygen() != null && report.getBloodOxygen() < 95) {
            score -= 15;
            concerns.add("血氧偏低");
        }

        // 评估体温
        if (report.getBodyTemperature() != null) {
            if (report.getBodyTemperature() > 37.5) {
                score -= 15;
                concerns.add("体温偏高");
            } else if (report.getBodyTemperature() < 36.0) {
                score -= 10;
                concerns.add("体温偏低");
            }
        }

        // 评估跌倒事件
        if (report.getFallDetected() != null && report.getFallDetected() > 0) {
            score -= 30;
            concerns.add("发生跌倒事件");
        }

        // 评估紧急呼叫
        if (report.getEmergencyCalls() != null && report.getEmergencyCalls() > 0) {
            score -= 20;
            concerns.add("有紧急呼叫");
        }

        // 确定健康状况
        if (score >= 90) {
            report.setHealthStatus(Report.HealthStatus.EXCELLENT);
        } else if (score >= 75) {
            report.setHealthStatus(Report.HealthStatus.GOOD);
        } else if (score >= 60) {
            report.setHealthStatus(Report.HealthStatus.FAIR);
        } else if (score >= 40) {
            report.setHealthStatus(Report.HealthStatus.POOR);
        } else {
            report.setHealthStatus(Report.HealthStatus.CRITICAL);
        }
    }

    private void generateAdvice(Report report) {
        StringBuilder healthAdvice = new StringBuilder();
        StringBuilder medicationAdvice = new StringBuilder();
        StringBuilder activityAdvice = new StringBuilder();

        // 健康建议
        if (report.getHeartRate() != null && (report.getHeartRate() < 60 || report.getHeartRate() > 100)) {
            healthAdvice.append("建议监测心率变化，如有不适及时就医。");
        }

        if (report.getBloodPressureSys() != null && report.getBloodPressureSys() > 140) {
            healthAdvice.append("血压偏高，建议低盐饮食，规律服药。");
        }

        if (report.getBloodOxygen() != null && report.getBloodOxygen() < 95) {
            healthAdvice.append("血氧饱和度偏低，建议适当休息，必要时吸氧。");
        }

        // 用药建议
        if (report.getMedicationMissed() != null && report.getMedicationMissed() > 0) {
            medicationAdvice.append(String.format("本周有%d次漏服药物，建议使用闹钟提醒功能。",
                    report.getMedicationMissed()));
        } else if (report.getMedicationTaken() != null && report.getMedicationTaken() > 0) {
            medicationAdvice.append("用药依从性良好，请继续保持。");
        }

        // 活动建议
        if (report.getStepCount() != null) {
            if (report.getStepCount() < 3000) {
                activityAdvice.append("活动量较少，建议每天适当散步。");
            } else if (report.getStepCount() > 10000) {
                activityAdvice.append("活动量充足，请注意适当休息。");
            } else {
                activityAdvice.append("活动量适中，继续保持。");
            }
        }

        if (report.getSleepDuration() != null) {
            if (report.getSleepDuration() < 6) {
                activityAdvice.append("睡眠时间不足，建议保证充足睡眠。");
            } else if (report.getSleepDuration() > 9) {
                activityAdvice.append("睡眠时间较长，建议适当增加日间活动。");
            }
        }

        report.setHealthAdvice(healthAdvice.length() > 0 ? healthAdvice.toString() : "健康状况良好。");
        report.setMedicationAdvice(medicationAdvice.length() > 0 ? medicationAdvice.toString() : "用药情况正常。");
        report.setActivityAdvice(activityAdvice.length() > 0 ? activityAdvice.toString() : "活动情况正常。");
    }

    @Override
    public ReportResponse updateReport(Long id, ReportRequest request) {
        log.info("Updating report with id: {}", id);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));

        dtoConverter.updateReportEntity(request, report);

        // 重新评估健康状况
        evaluateHealthStatus(report);
        generateAdvice(report);

        Report updatedReport = reportRepository.save(report);
        log.info("Report updated successfully");

        return dtoConverter.toReportResponse(updatedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long id) {
        log.info("Fetching report with id: {}", id);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));

        return dtoConverter.toReportResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReportResponse> queryReports(ReportQueryRequest request) {
        log.info("Querying reports with filters");

        Pageable pageable = createPageable(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "reportDate"));

        Page<Report> reportPage = reportRepository.findAll(pageable);

        List<ReportResponse> filteredResponses = filterAndConvertReports(
                reportPage.getContent(), request);

        return PageResponse.of(filteredResponses, request.getPage(), request.getSize(),
                (long) filteredResponses.size());
    }

    private List<ReportResponse> filterAndConvertReports(List<Report> reports, ReportQueryRequest request) {
        return reports.stream()
                .filter(r -> filterByDeviceId(r, request.getDeviceId()))
                .filter(r -> filterByUserId(r, request.getUserId()))
                .filter(r -> filterByReportType(r, request.getReportType()))
                .filter(r -> filterByHealthStatus(r, request.getHealthStatus()))
                .filter(r -> filterByIsRead(r, request.getIsRead()))
                .filter(r -> filterByDateRange(r, request.getStartDate(), request.getEndDate()))
                .map(dtoConverter::toReportResponse)
                .toList();
    }

    private boolean filterByDeviceId(Report report, Long deviceId) {
        return deviceId == null || report.getDevice().getId().equals(deviceId);
    }

    private boolean filterByUserId(Report report, Long userId) {
        return userId == null || report.getUser().getId().equals(userId);
    }

    private boolean filterByReportType(Report report, Report.ReportType type) {
        return type == null || report.getReportType() == type;
    }

    private boolean filterByHealthStatus(Report report, Report.HealthStatus healthStatus) {
        return healthStatus == null || report.getHealthStatus() == healthStatus;
    }

    private boolean filterByIsRead(Report report, Boolean isRead) {
        return isRead == null || report.getIsRead().equals(isRead);
    }

    private boolean filterByDateRange(Report report, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && report.getReportDate().isBefore(startDate)) return false;
        if (endDate != null && report.getReportDate().isAfter(endDate)) return false;
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByDeviceId(Long deviceId) {
        log.info("Fetching reports for device: {}", deviceId);

        List<Report> reports = reportRepository.findByDeviceIdOrderByReportDateDesc(deviceId);
        return dtoConverter.toReportResponseList(reports);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsByUserId(Long userId) {
        log.info("Fetching reports for user: {}", userId);

        List<Report> reports = reportRepository.findByUserIdOrderByReportDateDesc(userId);
        return dtoConverter.toReportResponseList(reports);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthTrendResponse getHealthTrend(Long userId, LocalDate startDate) {
        log.info("Getting health trend for user: {} from {}", userId, startDate);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Object[]> trendData = reportRepository.findHealthTrendData(userId, startDate);

        List<HealthTrendResponse.HealthDataPoint> dataPoints = new ArrayList<>();

        for (Object[] row : trendData) {
            LocalDate date = (LocalDate) row[0];
            Integer heartRate = (Integer) row[1];
            Integer bpSys = (Integer) row[2];
            Integer bpDia = (Integer) row[3];
            Double bodyTemp = (Double) row[4];
            Integer bloodOxygen = (Integer) row[5];

            dataPoints.add(HealthTrendResponse.HealthDataPoint.builder()
                    .date(date)
                    .heartRate(heartRate)
                    .bloodPressureSys(bpSys)
                    .bloodPressureDia(bpDia)
                    .bodyTemperature(bodyTemp)
                    .bloodOxygen(bloodOxygen)
                    .build());
        }

        // 计算趋势分析
        HealthTrendResponse.TrendAnalysis analysis = calculateTrendAnalysis(dataPoints);

        return HealthTrendResponse.builder()
                .userId(userId)
                .userName(user.getName())
                .healthData(dataPoints)
                .trendAnalysis(analysis)
                .build();
    }

    private HealthTrendResponse.TrendAnalysis calculateTrendAnalysis(
            List<HealthTrendResponse.HealthDataPoint> dataPoints) {

        if (dataPoints.isEmpty()) {
            return HealthTrendResponse.TrendAnalysis.builder().build();
        }

        double avgHeartRate = dataPoints.stream()
                .filter(d -> d.getHeartRate() != null)
                .mapToInt(HealthTrendResponse.HealthDataPoint::getHeartRate)
                .average().orElse(0);

        double avgBodyTemp = dataPoints.stream()
                .filter(d -> d.getBodyTemperature() != null)
                .mapToDouble(HealthTrendResponse.HealthDataPoint::getBodyTemperature)
                .average().orElse(36.5);

        double avgBloodOxygen = dataPoints.stream()
                .filter(d -> d.getBloodOxygen() != null)
                .mapToInt(HealthTrendResponse.HealthDataPoint::getBloodOxygen)
                .average().orElse(98);

        String bpTrend = "stable";
        List<HealthTrendResponse.HealthDataPoint> validBpData = dataPoints.stream()
                .filter(d -> d.getBloodPressureSys() != null)
                .toList();

        if (validBpData.size() >= 2) {
            HealthTrendResponse.HealthDataPoint first = validBpData.get(0);
            HealthTrendResponse.HealthDataPoint last = validBpData.get(validBpData.size() - 1);

            if (last.getBloodPressureSys() > first.getBloodPressureSys() + 5) {
                bpTrend = "rising";
            } else if (last.getBloodPressureSys() < first.getBloodPressureSys() - 5) {
                bpTrend = "falling";
            }
        }

        Map<String, String> suggestions = new HashMap<>();
        if (avgHeartRate > 100 || avgHeartRate < 60) {
            suggestions.put("heart_rate", "建议关注心率变化");
        }
        if ("rising".equals(bpTrend)) {
            suggestions.put("blood_pressure", "血压呈上升趋势，建议监测");
        }
        if (avgBloodOxygen < 95) {
            suggestions.put("blood_oxygen", "血氧偏低，建议关注");
        }

        return HealthTrendResponse.TrendAnalysis.builder()
                .avgHeartRate(avgHeartRate)
                .bloodPressureTrend(bpTrend)
                .avgBodyTemperature(avgBodyTemp)
                .avgBloodOxygen(avgBloodOxygen)
                .healthSuggestions(suggestions)
                .build();
    }

    @Override
    public ReportResponse generateDailyHealthReport(Long userId, Long deviceId) {
        log.info("Generating daily health report for user: {}, device: {}", userId, deviceId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        Report report = new Report();
        report.setReportType(Report.ReportType.DAILY_HEALTH);
        report.setTitle(user.getName() + " " + today + " 健康日报");
        report.setDevice(device);
        report.setUser(user);
        report.setStartDate(today);
        report.setEndDate(today);
        report.setReportDate(today);
        report.setGeneratedBy(0L); // 系统生成

        // 收集今日数据
        collectDailyData(report, user, device, todayStart, todayEnd);

        // 生成内容
        StringBuilder content = new StringBuilder();
        content.append(String.format("【%s 健康日报】\n\n", today));
        content.append(String.format("心率：%d bpm\n", report.getHeartRate() != null ? report.getHeartRate() : 0));
        content.append(String.format("血压：%d/%d mmHg\n",
                report.getBloodPressureSys() != null ? report.getBloodPressureSys() : 0,
                report.getBloodPressureDia() != null ? report.getBloodPressureDia() : 0));
        content.append(String.format("体温：%.1f ℃\n",
                report.getBodyTemperature() != null ? report.getBodyTemperature() : 0.0));
        content.append(String.format("血氧：%d %%\n",
                report.getBloodOxygen() != null ? report.getBloodOxygen() : 0));
        content.append(String.format("步数：%d 步\n",
                report.getStepCount() != null ? report.getStepCount() : 0));
        content.append(String.format("用药：按时%d次，漏服%d次\n",
                report.getMedicationTaken() != null ? report.getMedicationTaken() : 0,
                report.getMedicationMissed() != null ? report.getMedicationMissed() : 0));

        report.setContent(content.toString());

        // 评估健康状况
        evaluateHealthStatus(report);
        generateAdvice(report);

        Report savedReport = reportRepository.save(report);
        log.info("Daily health report generated successfully");

        return dtoConverter.toReportResponse(savedReport);
    }

    private void collectDailyData(Report report, User user, Device device,
                                  LocalDateTime startTime, LocalDateTime endTime) {
        // 收集用药数据
        List<Reminder> reminders = reminderRepository.findByUserIdOrderByRemindTimeAsc(user.getId());
        long taken = reminders.stream()
                .filter(r -> r.getReminderType() == Reminder.ReminderType.MEDICATION)
                .filter(r -> r.getTakenTime() != null &&
                        !r.getTakenTime().isBefore(startTime) &&
                        r.getTakenTime().isBefore(endTime))
                .count();
        long missed = reminders.stream()
                .filter(r -> r.getReminderType() == Reminder.ReminderType.MEDICATION)
                .filter(r -> r.getIsActive() && !r.getIsTaken())
                .count();

        report.setMedicationTaken((int) taken);
        report.setMedicationMissed((int) missed);

        // 收集紧急事件数据
        List<Emergency> emergencies = emergencyRepository.findByUserIdOrderByTriggerTimeDesc(user.getId());
        long emergencyCalls = emergencies.stream()
                .filter(e -> !e.getTriggerTime().isBefore(startTime) && e.getTriggerTime().isBefore(endTime))
                .count();
        long falls = emergencies.stream()
                .filter(e -> e.getEmergencyType() == Emergency.EmergencyType.FALL_DETECTION)
                .filter(e -> !e.getTriggerTime().isBefore(startTime) && e.getTriggerTime().isBefore(endTime))
                .count();

        report.setEmergencyCalls((int) emergencyCalls);
        report.setFallDetected((int) falls);
    }

    @Override
    public void markReportAsRead(Long id, Long readBy) {
        log.info("Marking report {} as read by {}", id, readBy);

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", id));

        report.setIsRead(true);
        report.setReadBy(readBy);
        report.setReadTime(LocalDateTime.now());

        reportRepository.save(report);
        log.info("Report marked as read successfully");
    }

    @Override
    public void deleteReport(Long id) {
        log.info("Deleting report with id: {}", id);

        if (!reportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report", "id", id);
        }

        reportRepository.deleteById(id);
        log.info("Report deleted successfully");
    }

    /**
     * 定时任务：每天凌晨1点为所有老年人用户生成健康日报
     */
    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void scheduledGenerateDailyReports() {
        log.info("Running scheduled daily health report generation");

        List<User> elderlyUsers = userRepository.findByRoleOrderByNameAsc(User.Role.ELDERLY);
        int generatedCount = 0;

        for (User user : elderlyUsers) {
            if (user.getDeviceId() != null) {
                try {
                    generateDailyHealthReport(user.getId(), user.getDeviceId());
                    generatedCount++;
                } catch (Exception e) {
                    log.error("Failed to generate daily report for user: {}", user.getId(), e);
                }
            }
        }

        log.info("Generated {} daily health reports", generatedCount);
    }
}