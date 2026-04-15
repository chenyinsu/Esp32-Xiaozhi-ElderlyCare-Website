package com.example.esp32_robot.scheduler;

package com.example.esp32robot.scheduler;

import com.example.esp32_robot.entity.Reminder;
import com.example.esp32_robot.service.ReminderService;
import com.example.esp32_robot.websocket.DeviceWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        List<Reminder> dueReminders = reminderService.getDueReminders();

        for (Reminder reminder : dueReminders) {
            try {
                // 发送提醒到设备
                Map<String, Object> reminderMessage = Map.of(
                        "id", reminder.getId(),
                        "content", reminder.getContent(),
                        "time", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        "priority", reminder.getPriority()
                );

                deviceWebSocketHandler.sendReminder(
                        reminder.getDevice().getDeviceId(),
                        reminderMessage
                );

                // 更新提醒状态
                reminderService.triggerReminder(reminder.getId());

                log.info("已发送提醒 {} 到设备 {}", reminder.getId(), reminder.getDevice().getDeviceId());
            } catch (Exception e) {
                log.error("发送提醒失败: {}", e.getMessage(), e);
            }
        }
    }
}