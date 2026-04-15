package com.example.esp32_robot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@CrossOrigin
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @GetMapping
    public ApiResponse<List<ReminderDTO>> getReminders(@RequestParam Long userId) {
        List<ReminderDTO> reminders = reminderService.getRemindersByUserId(userId);
        return ApiResponse.success(reminders);
    }

    @PostMapping
    public ApiResponse<ReminderDTO> createReminder(@RequestBody ReminderDTO dto) {
        ReminderDTO created = reminderService.createReminder(dto);
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}")
    public ApiResponse<ReminderDTO> updateReminder(
            @PathVariable Long id,
            @RequestBody ReminderDTO dto) {
        ReminderDTO updated = reminderService.updateReminder(id, dto);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteReminder(@PathVariable Long id) {
        reminderService.deleteReminder(id);
        return ApiResponse.success("删除成功");
    }

    @PostMapping("/{id}/trigger")
    public ApiResponse<String> triggerReminder(@PathVariable Long id) {
        reminderService.triggerReminderImmediate(id);
        return ApiResponse.success("已立即触发提醒");
    }
}