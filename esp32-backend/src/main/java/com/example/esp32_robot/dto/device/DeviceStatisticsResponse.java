package com.example.esp32_robot.dto.device;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceStatisticsResponse {
    private Long totalDevices;
    private Long onlineDevices;
    private Long offlineDevices;
    private Long maintenanceDevices;
    private Long errorDevices;
    private Long lowBatteryDevices;
    private Long unboundDevices;
    private Long cameraDevices;
}