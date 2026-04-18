package com.example.esp32_robot.service;

import com.example.esp32_robot.dto.reminder.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.entity.Reminder;

import java.util.List;

public interface ReminderService {

    // ==================== 基础 CRUD ====================

    ReminderResponse createReminder(ReminderRequest request);

    ReminderResponse updateReminder(Long id, ReminderRequest request);

    ReminderResponse getReminderById(Long id);

    void deleteReminder(Long id);

    // ==================== 查询方法 ====================

    PageResponse<ReminderResponse> queryReminders(ReminderQueryRequest request);

    List<ReminderResponse> getRemindersByDeviceId(Long deviceId);

    List<ReminderResponse> getRemindersByUserId(Long userId);

    List<ReminderResponse> getTodayReminders();

    List<ReminderResponse> getRemindersToTrigger();

    // ✅ 添加：获取到期的提醒（返回实体列表，供定时任务使用）
    List<Reminder> getDueReminders();

    // ==================== 业务方法 ====================

    // ✅ 添加：触发提醒
    void triggerReminder(Long reminderId);

    ReminderResponse markReminderAsTaken(Long id);

    MedicationTrackingResponse getMedicationTracking(Long userId);

    void batchUpdateReminderStatus(List<Long> ids, Boolean isActive);
}