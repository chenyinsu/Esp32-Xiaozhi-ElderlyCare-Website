package com.example.esp32_robot.scheduler;

import com.example.esp32_robot.entity.Reminder;
import com.example.esp32_robot.service.ReminderService;
import com.example.esp32_robot.websocket.DeviceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderService reminderService;
    private final DeviceWebSocketHandler deviceWebSocketHandler;

    @Scheduled(fixedDelay = 30000) // 每30秒检查一次
    public void checkReminders() {
        log.debug("Checking due reminders...");
        List<Reminder> dueReminders = reminderService.getDueReminders();

        if (dueReminders.isEmpty()) {
            return;
        }

        log.info("Found {} due reminders", dueReminders.size());

        for (Reminder reminder : dueReminders) {
            try {
                // 构建提醒消息
                Map<String, Object> reminderMessage = new HashMap<>();
                reminderMessage.put("id", reminder.getId());
                reminderMessage.put("title", reminder.getTitle());
                reminderMessage.put("content", reminder.getContent());
                reminderMessage.put("time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                reminderMessage.put("priority", reminder.getPriority() != null ?
                        reminder.getPriority().name() : "MEDIUM");
                reminderMessage.put("type", reminder.getReminderType().name());

                // 发送提醒到设备
                String deviceId = reminder.getDevice().getDeviceId();
                deviceWebSocketHandler.sendReminder(deviceId, reminderMessage);

                // 更新提醒状态
                reminderService.triggerReminder(reminder.getId());

                log.info("已发送提醒 {} 到设备 {}", reminder.getId(), deviceId);
            } catch (Exception e) {
                log.error("发送提醒 {} 失败: {}", reminder.getId(), e.getMessage(), e);
            }
        }
    }
}