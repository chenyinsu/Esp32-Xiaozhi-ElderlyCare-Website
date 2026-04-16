package com.example.esp32_robot.dto.reminder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationTrackingResponse {
    private Long userId;
    private String userName;
    private Long totalReminders;
    private Long takenCount;
    private Long missedCount;
    private Double adherenceRate;
    private Map<LocalDate, Boolean> dailyAdherence;
}