package com.example.esp32_robot.service;

import com.example.esp32_robot.entity.Reminder;
import com.example.esp32_robot.repository.ReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {
    private final ReminderRepository reminderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> deviceSessions = new ConcurrentHashMap<>();

    // 注册设备WebSocket连接
    public void registerDeviceSession(String deviceId, WebSocketSession session) {
        deviceSessions.put(deviceId, session);
        log.info("设备 {} WebSocket连接建立", deviceId);
    }

    // 移除设备连接
    public void removeDeviceSession(String deviceId) {
        deviceSessions.remove(deviceId);
        log.info("设备 {} WebSocket连接断开", deviceId);
    }

    // 创建提醒
    public Reminder createReminder(Reminder reminder) {
        reminder.setNextTriggerTime(calculateNextTriggerTime(reminder));
        return reminderRepository.save(reminder);
    }

    // 计算下次触发时间
    private LocalDateTime calculateNextTriggerTime(Reminder reminder) {
        LocalDateTime now = LocalDateTime.now();
        LocalTime triggerTime = reminder.getReminderTime();

        if ("once".equals(reminder.getRepeat())) {
            return LocalDateTime.of(now.toLocalDate(), triggerTime);
        } else if ("daily".equals(reminder.getRepeat())) {
            LocalDateTime todayTrigger = LocalDateTime.of(now.toLocalDate(), triggerTime);
            return todayTrigger.isAfter(now) ? todayTrigger : todayTrigger.plusDays(1);
        }
        return now.plusHours(1);
    }

    // 定时检查提醒
    @Scheduled(fixedRate = 30000) // 每30秒检查一次
    public void checkDueReminders() {
        List<Reminder> dueReminders = reminderRepository.findDueReminders(LocalDateTime.now());

        for (Reminder reminder : dueReminders) {
            sendReminderToDevice(reminder);
            updateNextTriggerTime(reminder);
        }
    }

    // 发送提醒到设备
    private void sendReminderToDevice(Reminder reminder) {
        String deviceId = reminder.getDeviceId();
        WebSocketSession session = deviceSessions.get(deviceId);

        if (session != null && session.isOpen()) {
            try {
                Map<String, Object> message = new HashMap<>();
                message.put("event", "reminder");
                message.put("reminderId", reminder.getId());
                message.put("content", reminder.getContent());
                message.put("time", LocalDateTime.now().toString());
                message.put("priority", "high");

                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));

                log.info("向设备 {} 发送提醒: {}", deviceId, reminder.getContent());
            } catch (IOException e) {
                log.error("发送提醒到设备 {} 失败", deviceId, e);
            }
        } else {
            log.warn("设备 {} 未连接WebSocket，提醒无法实时送达", deviceId);
        }
    }

    // 更新下次触发时间
    private void updateNextTriggerTime(Reminder reminder) {
        if ("daily".equals(reminder.getRepeat())) {
            reminder.setNextTriggerTime(reminder.getNextTriggerTime().plusDays(1));
        } else if ("once".equals(reminder.getRepeat())) {
            reminder.setEnabled(false);
        }
        reminderRepository.save(reminder);
    }

    public List<Reminder> getRemindersByDevice(String deviceId) {
        return reminderRepository.findByDeviceId(deviceId);
    }

    public void deleteReminder(Long id) {
        reminderRepository.deleteById(id);
    }
}