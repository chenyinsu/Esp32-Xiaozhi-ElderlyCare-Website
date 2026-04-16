package com.example.esp32_robot.dto.emergency;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyStatisticsResponse {
    private Long totalEmergencies;
    private Long pendingCount;
    private Long handlingCount;
    private Long resolvedCount;
    private Long closedCount;
    private Long falseAlarmCount;
    private Long todayCount;
    private Long thisWeekCount;
    private Double averageResponseTime; // 分钟
    private Map<String, Long> countByType;
    private Map<String, Long> countByLevel;
}