package com.example.esp32_robot.controller;

package com.example.esp32robot.controller;

import com.example.esp32_robot.dto.ApiResponse;
import com.example.esp32_robot.dto.ReminderDTO;
import com.example.esp32_robot.entity.Reminder;
import com.example.esp32_robot.service.ReminderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {
    private final ReminderService reminderService;

    @PostMapping
    public ApiResponse<Reminder> createReminder(@Valid @RequestBody ReminderDTO dto) {
        Reminder reminder = reminderService.createReminder(dto);
        return ApiResponse.success(reminder);
    }

    @GetMapping
    public ApiResponse<List<Reminder>> getReminders(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) Long userId) {

        List<Reminder> reminders;
        if (deviceId != null) {
            reminders = reminderService.getRemindersByDevice(deviceId);
        } else if (userId != null) {
            reminders = reminderService.getRemindersByUser(userId);
        } else {
            reminders = reminderService.getDueReminders();
        }

        return ApiResponse.success(reminders);
    }

    @GetMapping("/{id}")
    public ApiResponse<Reminder> getReminder(@PathVariable Long id) {
        Reminder reminder = reminderService.getReminder(id);
        return ApiResponse.success(reminder);
    }

    @PutMapping("/{id}")
    public ApiResponse<Reminder> updateReminder(
            @PathVariable Long id,
            @Valid @RequestBody ReminderDTO dto) {
        Reminder reminder = reminderService.updateReminder(id, dto);
        return ApiResponse.success(reminder);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/trigger")
    public ApiResponse<Void> triggerReminder(@PathVariable Long id) {
        reminderService.triggerReminder(id);
        return ApiResponse.success(null);
    }
}