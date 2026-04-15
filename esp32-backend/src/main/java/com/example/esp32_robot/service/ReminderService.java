package com.example.esp32_robot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ReminderService {

    @Autowired
    private ReminderRepository reminderRepository;

    @Autowired
    private DeviceWebSocketHandler deviceWebSocketHandler;

    public List<ReminderDTO> getRemindersByUserId(Long userId) {
        // 从数据库查询
        List<Reminder> reminders = reminderRepository.findByUserId(userId);
        return reminders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ReminderDTO createReminder(ReminderDTO dto) {
        Reminder reminder = new Reminder();
        reminder.setUserId(dto.getUserId());
        reminder.setDeviceId(dto.getDeviceId());
        reminder.setContent(dto.getContent());
        reminder.setReminderTime(dto.getReminderTime());
        reminder.setRepeat(dto.getRepeat());
        reminder.setEnabled(true);
        reminder.setCreatedAt(LocalDateTime.now());

        Reminder saved = reminderRepository.save(reminder);
        return convertToDTO(saved);
    }

    public void triggerReminderImmediate(Long reminderId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new RuntimeException("提醒不存在"));

        // 立即推送给硬件
        String deviceId = reminder.getDeviceId();
        Map<String, Object> message = new HashMap<>();
        message.put("event", "reminder");
        message.put("reminderId", reminderId);
        message.put("content", reminder.getContent());
        message.put("time", LocalDateTime.now());

        deviceWebSocketHandler.sendToDevice(deviceId, message);
    }

    private ReminderDTO convertToDTO(Reminder reminder) {
        ReminderDTO dto = new ReminderDTO();
        dto.setId(reminder.getId());
        // ... 其他字段映射
        return dto;
    }
}