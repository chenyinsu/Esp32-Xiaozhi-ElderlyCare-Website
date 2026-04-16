package com.example.esp32_robot.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityResponse {
    private List<RecentEmergency> recentEmergencies;
    private List<RecentReminder> recentReminders;
    private List<RecentChat> recentChats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentEmergency {
        private Long id;
        private String userName;
        private String emergencyType;
        private String emergencyLevel;
        private String status;
        private LocalDateTime triggerTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentReminder {
        private Long id;
        private String userName;
        private String title;
        private Boolean isTaken;
        private LocalDateTime remindTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentChat {
        private Long id;
        private String userName;
        private String content;
        private String mood;
        private LocalDateTime timestamp;
    }
}