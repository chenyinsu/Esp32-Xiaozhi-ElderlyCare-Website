package com.example.esp32_robot.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewResponse {
    private Long totalUsers;
    private Long totalDevices;
    private Long onlineDevices;
    private Long pendingEmergencies;
    private Long todayReminders;
    private Long unreadNotifications;
    private Long lowBatteryDevices;
    private Long todayChats;
}