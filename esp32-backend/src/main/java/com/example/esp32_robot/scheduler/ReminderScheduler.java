package com.example.esp32_robot.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class ReminderScheduler {

    @Autowired
    private ReminderService reminderService;

    // 每分钟检查一次是否有需要触发的提醒
    @Scheduled(fixedRate = 60000)
    public void checkAndTriggerReminders() {
        reminderService.checkPendingReminders();
    }
}