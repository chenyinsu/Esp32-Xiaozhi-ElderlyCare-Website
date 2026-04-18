package com.example.esp32_robot.controller;

import com.example.esp32_robot.dto.ApiResponse;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.reminder.*;
import com.example.esp32_robot.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;

    @PostMapping
    public ApiResponse<ReminderResponse> createReminder(@Valid @RequestBody ReminderRequest request) {
        log.info("REST request to create reminder");
        ReminderResponse response = reminderService.createReminder(request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<ReminderResponse> updateReminder(
            @PathVariable Long id,
            @Valid @RequestBody ReminderRequest request) {
        log.info("REST request to update reminder: {}", id);
        ReminderResponse response = reminderService.updateReminder(id, request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<ReminderResponse> getReminder(@PathVariable Long id) {
        log.info("REST request to get reminder: {}", id);
        ReminderResponse response = reminderService.getReminderById(id);
        return ApiResponse.success(response);
    }

    @PostMapping("/query")
    public ApiResponse<PageResponse<ReminderResponse>> queryReminders(@RequestBody ReminderQueryRequest request) {
        log.info("REST request to query reminders");
        PageResponse<ReminderResponse> response = reminderService.queryReminders(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/device/{deviceId}")
    public ApiResponse<List<ReminderResponse>> getRemindersByDeviceId(@PathVariable Long deviceId) {
        log.info("REST request to get reminders for device: {}", deviceId);
        List<ReminderResponse> responses = reminderService.getRemindersByDeviceId(deviceId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<ReminderResponse>> getRemindersByUserId(@PathVariable Long userId) {
        log.info("REST request to get reminders for user: {}", userId);
        List<ReminderResponse> responses = reminderService.getRemindersByUserId(userId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/today")
    public ApiResponse<List<ReminderResponse>> getTodayReminders() {
        log.info("REST request to get today's reminders");
        List<ReminderResponse> responses = reminderService.getTodayReminders();
        return ApiResponse.success(responses);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReminder(@PathVariable Long id) {
        log.info("REST request to delete reminder: {}", id);
        reminderService.deleteReminder(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/trigger")
    public ApiResponse<Void> triggerReminder(@PathVariable Long id) {
        log.info("REST request to trigger reminder: {}", id);
        reminderService.triggerReminder(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/taken")
    public ApiResponse<ReminderResponse> markReminderAsTaken(@PathVariable Long id) {
        log.info("REST request to mark reminder as taken: {}", id);
        ReminderResponse response = reminderService.markReminderAsTaken(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/medication-tracking/{userId}")
    public ApiResponse<MedicationTrackingResponse> getMedicationTracking(@PathVariable Long userId) {
        log.info("REST request to get medication tracking for user: {}", userId);
        MedicationTrackingResponse response = reminderService.getMedicationTracking(userId);
        return ApiResponse.success(response);
    }
}