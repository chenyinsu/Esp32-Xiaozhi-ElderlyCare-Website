package com.example.esp32robot.controller;

import com.example.esp32_robot.dto.ApiResponse;
import com.example.esp32_robot.dto.EmergencyDTO;
import com.example.esp32_robot.entity.Emergency;
import com.example.esp32_robot.service.EmergencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/emergencies")
@RequiredArgsConstructor
public class EmergencyController {
    private final EmergencyService emergencyService;

    @PostMapping
    public ApiResponse<Emergency> createEmergency(@Valid @RequestBody EmergencyDTO dto) {
        Emergency emergency = emergencyService.createEmergency(dto);
        return ApiResponse.success(emergency);
    }

    @GetMapping
    public ApiResponse<List<Emergency>> getEmergencies(
            @RequestParam(required = false) String deviceId) {

        List<Emergency> emergencies;
        if (deviceId != null) {
            emergencies = emergencyService.getEmergenciesByDevice(deviceId);
        } else {
            emergencies = emergencyService.getPendingEmergencies();
        }

        return ApiResponse.success(emergencies);
    }

    @GetMapping("/{id}")
    public ApiResponse<Emergency> getEmergency(@PathVariable Long id) {
        Emergency emergency = emergencyService.getEmergency(id);
        return ApiResponse.success(emergency);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Emergency> updateEmergencyStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        Emergency emergency = emergencyService.updateEmergencyStatus(id, status);
        return ApiResponse.success(emergency);
    }

    @PostMapping("/{id}/notify")
    public ApiResponse<Void> notifyEmergency(
            @PathVariable Long id,
            @RequestParam String notificationType,
            @RequestParam String recipients,
            @RequestParam String message) {

        emergencyService.notifyEmergency(id, notificationType, recipients, message);
        return ApiResponse.success(null);
    }
}