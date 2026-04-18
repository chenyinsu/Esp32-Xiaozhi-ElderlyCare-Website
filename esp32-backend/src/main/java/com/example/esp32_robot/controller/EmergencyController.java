package com.example.esp32_robot.controller;

import com.example.esp32_robot.dto.ApiResponse;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.emergency.*;
import com.example.esp32_robot.service.EmergencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/emergencies")
@RequiredArgsConstructor
public class EmergencyController {

    private final EmergencyService emergencyService;

    @PostMapping
    public ApiResponse<EmergencyResponse> createEmergency(@Valid @RequestBody EmergencyRequest request) {
        log.info("REST request to create emergency for device: {}", request.getDeviceId());
        EmergencyResponse response = emergencyService.createEmergency(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<EmergencyResponse> getEmergencyById(@PathVariable Long id) {
        log.info("REST request to get emergency by id: {}", id);
        EmergencyResponse response = emergencyService.getEmergencyById(id);
        return ApiResponse.success(response);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<EmergencyResponse> updateEmergencyStatus(
            @PathVariable Long id,
            @Valid @RequestBody EmergencyHandleRequest request) {
        log.info("REST request to update emergency status: {} to {}", id, request.getStatus());
        EmergencyResponse response = emergencyService.updateEmergencyStatus(id, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/query")
    public ApiResponse<PageResponse<EmergencyResponse>> queryEmergencies(
            @RequestBody EmergencyQueryRequest request) {
        log.info("REST request to query emergencies");
        PageResponse<EmergencyResponse> response = emergencyService.queryEmergencies(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/pending")
    public ApiResponse<List<EmergencyResponse>> getPendingEmergencies() {
        log.info("REST request to get pending emergencies");
        List<EmergencyResponse> responses = emergencyService.getPendingEmergencies();
        return ApiResponse.success(responses);
    }

    @GetMapping("/device/{deviceId}")
    public ApiResponse<List<EmergencyResponse>> getEmergenciesByDeviceId(@PathVariable Long deviceId) {
        log.info("REST request to get emergencies for device: {}", deviceId);
        List<EmergencyResponse> responses = emergencyService.getEmergenciesByDeviceId(deviceId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<EmergencyResponse>> getEmergenciesByUserId(@PathVariable Long userId) {
        log.info("REST request to get emergencies for user: {}", userId);
        List<EmergencyResponse> responses = emergencyService.getEmergenciesByUserId(userId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/recent")
    public ApiResponse<List<EmergencyResponse>> getRecentEmergencies(
            @RequestParam(defaultValue = "24") int hours) {
        log.info("REST request to get recent emergencies from last {} hours", hours);
        List<EmergencyResponse> responses = emergencyService.getRecentEmergencies(hours);
        return ApiResponse.success(responses);
    }

    @GetMapping("/statistics")
    public ApiResponse<EmergencyStatisticsResponse> getEmergencyStatistics() {
        log.info("REST request to get emergency statistics");
        EmergencyStatisticsResponse response = emergencyService.getEmergencyStatistics();
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteEmergency(@PathVariable Long id) {
        log.info("REST request to delete emergency: {}", id);
        emergencyService.deleteEmergency(id);
        return ApiResponse.success(null);
    }
}