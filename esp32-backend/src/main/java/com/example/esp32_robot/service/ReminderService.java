package com.example.esp32_robot.service;

import com.example.esp32_robot.dto.reminder.*;
import com.example.esp32_robot.dto.common.PageResponse;

import java.util.List;

public interface ReminderService {

    ReminderResponse createReminder(ReminderRequest request);

    ReminderResponse updateReminder(Long id, ReminderRequest request);

    ReminderResponse getReminderById(Long id);

    PageResponse<ReminderResponse> queryReminders(ReminderQueryRequest request);
    List<ReminderResponse> getRemindersByDeviceId(Long deviceId);

    List<ReminderResponse> getRemindersByUserId(Long userId);

    List<ReminderResponse> getTodayReminders();

    List<ReminderResponse> getRemindersToTrigger();

    ReminderResponse markReminderAsTaken(Long id);

    MedicationTrackingResponse getMedicationTracking(Long userId);

    void deleteReminder(Long id);

    void batchUpdateReminderStatus(List<Long> ids, Boolean isActive);
}