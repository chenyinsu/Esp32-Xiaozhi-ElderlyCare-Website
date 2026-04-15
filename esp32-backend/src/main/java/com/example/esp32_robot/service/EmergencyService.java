package com.example.esp32_robot.service;

import com.example.esp32_robot.entity.Emergency;
import com.example.esp32_robot.repository.EmergencyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmergencyService {
    private final EmergencyRepository emergencyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> adminSessions = new ConcurrentHashMap<>();

    // 注册管理端WebSocket连接
    public void registerAdminSession(String sessionId, WebSocketSession session) {
        adminSessions.put(sessionId, session);
        log.info("管理端连接建立: {}", sessionId);
    }

    // 移除管理端连接
    public void removeAdminSession(String sessionId) {
        adminSessions.remove(sessionId);
        log.info("管理端连接断开: {}", sessionId);
    }

    // 上报紧急事件
    public Emergency reportEmergency(Emergency emergency) {
        emergency.setCreatedAt(LocalDateTime.now());
        Emergency saved = emergencyRepository.save(emergency);
        broadcastEmergencyAlert(saved);
        return saved;
    }

    // 广播紧急警报
    private void broadcastEmergencyAlert(Emergency emergency) {
        Map<String, Object> alert = new HashMap<>();
        alert.put("event", "emergency_alert");
        alert.put("emergencyId", emergency.getId());
        alert.put("deviceId", emergency.getDeviceId());
        alert.put("level", emergency.getLevel());
        alert.put("message", String.format("设备 %s 发生紧急事件!", emergency.getDeviceId()));
        alert.put("timestamp", emergency.getTimestamp().toString());
        alert.put("videoUrl", String.format("http://backend/video/%s/live", emergency.getDeviceId()));
        alert.put("actions", List.of("view_video", "contact_family", "call_hospital"));

        try {
            String jsonAlert = objectMapper.writeValueAsString(alert);
            TextMessage message = new TextMessage(jsonAlert);

            for (WebSocketSession session : adminSessions.values()) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
            log.info("向管理端广播紧急警报: {}", emergency.getDeviceId());
        } catch (IOException e) {
            log.error("广播紧急警报失败", e);
        }
    }

    public List<Emergency> getEmergenciesByDevice(String deviceId) {
        return emergencyRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId);
    }

    public void markAsNotified(Long id) {
        emergencyRepository.findById(id).ifPresent(emergency -> {
            emergency.setNotified(true);
            emergencyRepository.save(emergency);
        });
    }
}