package com.example.esp32_robot.websocket;

import com.example.esp32_robot.entity.Emergency;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
@Slf4j
public class AlertWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CopyOnWriteArrayList<WebSocketSession> adminSessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        adminSessions.add(session);
        log.info("管理员客户端已连接WebSocket");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        adminSessions.remove(session);
        log.info("管理员客户端已断开WebSocket连接");
    }

    public void sendEmergencyAlert(Emergency emergency) {
        Map<String, Object> message = Map.of(
                "event", "emergency_alert",
                "emergencyId", emergency.getId(),
                "deviceId", emergency.getDevice().getDeviceId(),
                "level", emergency.getLevel(),
                "message", "设备 " + emergency.getDevice().getDeviceId() + " 发生紧急事件！",
                "timestamp", emergency.getTimestamp().toString(),
                "videoUrl", "http://backend/video/" + emergency.getDevice().getDeviceId() + "/live",
                "actions", new String[]{"view_video", "contact_family", "call_hospital"}
        );

        broadcastMessage(message);
    }

    private void broadcastMessage(Map<String, Object> message) {
        for (WebSocketSession session : adminSessions) {
            if (session.isOpen()) {
                try {
                    String json = objectMapper.writeValueAsString(message);
                    session.sendMessage(new TextMessage(json));
                } catch (IOException e) {
                    log.error("发送告警消息失败: {}", e.getMessage());
                }
            }
        }
    }
}