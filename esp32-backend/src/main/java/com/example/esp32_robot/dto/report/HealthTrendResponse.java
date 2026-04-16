package com.example.esp32_robot.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthTrendResponse {
    private Long userId;
    private String userName;
    private List<HealthDataPoint> healthData;
    private TrendAnalysis trendAnalysis;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthDataPoint {
        private LocalDate date;
        private Integer heartRate;
        private Integer bloodPressureSys;
        private Integer bloodPressureDia;
        private Double bodyTemperature;
        private Integer bloodOxygen;
        private Integer stepCount;
        private Double sleepDuration;
        private Integer medicationTaken;
        private Integer medicationMissed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendAnalysis {
        private Double avgHeartRate;
        private String bloodPressureTrend;
        private Double avgBodyTemperature;
        private Double avgBloodOxygen;
        private Integer totalSteps;
        private Double avgSleepDuration;
        private Double medicationAdherenceRate;
        private Map<String, String> healthSuggestions;
    }
}