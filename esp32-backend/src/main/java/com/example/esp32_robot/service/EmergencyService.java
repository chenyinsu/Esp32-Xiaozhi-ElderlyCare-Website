package com.example.esp32_robot.service;

import com.example.esp32_robot.dto.emergency.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.entity.Emergency;

import java.time.LocalDateTime;
import java.util.List;

public interface EmergencyService {

    EmergencyResponse createEmergency(EmergencyRequest request);

    EmergencyResponse getEmergencyById(Long id);

    EmergencyResponse updateEmergencyStatus(Long id, EmergencyHandleRequest request);

    PageResponse<EmergencyResponse> queryEmergencies(EmergencyQueryRequest request);

    List<EmergencyResponse> getPendingEmergencies();

    List<EmergencyResponse> getEmergenciesByDeviceId(Long deviceId);

    List<EmergencyResponse> getEmergenciesByUserId(Long userId);

    List<EmergencyResponse> getRecentEmergencies(int hours);

    EmergencyStatisticsResponse getEmergencyStatistics();

    void deleteEmergency(Long id);
}
