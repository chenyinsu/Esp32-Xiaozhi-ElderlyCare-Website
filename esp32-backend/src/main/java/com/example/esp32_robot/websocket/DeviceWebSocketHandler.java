package com.example.esp32_robot.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriTemplate;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class DeviceWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WebSocketSession> deviceSessions = new ConcurrentHashMap<>();
    private final UriTemplate uriTemplate = new UriTemplate("/ws/device/{deviceId}");

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String deviceId = extractDeviceId(session);
        if (deviceId != null) {
            deviceSessions.put(deviceId, session);
            log.info("设备 {} 已连接WebSocket", deviceId);

            // 发送连接成功消息
            Map<String, Object> message = Map.of(
                    "event", "connected",
                    "message", "WebSocket连接已建立",
                    "deviceId", deviceId
            );
            sendMessage(deviceId, message);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String deviceId = extractDeviceId(session);
        if (deviceId != null) {
            String payload = message.getPayload();
            log.info("收到设备 {} 的消息: {}", deviceId, payload);

            // 处理设备消息（如提醒确认、心跳等）
            Map<String, Object> data = objectMapper.readValue(payload, Map.class);
            String event = (String) data.get("event");

            switch (event) {
                case "heartbeat":
                    handleHeartbeat(deviceId, data);
                    break;
                case "reminder_ack":
                    handleReminderAck(deviceId, data);
                    break;
                case "chat_message":
                    handleChatMessage(deviceId, data);
                    break;
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String deviceId = extractDeviceId(session);
        if (deviceId != null) {
            deviceSessions.remove(deviceId);
            log.info("设备 {} 已断开WebSocket连接", deviceId);
        }
    }

    public void sendReminder(String deviceId, Map<String, Object> reminder) {
        Map<String, Object> message = Map.of(
                "event", "reminder",
                "reminderId", reminder.get("id"),
                "content", reminder.get("content"),
                "time", reminder.get("time"),
                "priority", reminder.get("priority")
        );
        sendMessage(deviceId, message);
    }

    public void sendChatResponse(String deviceId, String content, boolean isComplete) {
        Map<String, Object> message = Map.of(
                "event", "chat_response",
                "content", content,
                "isComplete", isComplete
        );
        sendMessage(deviceId, message);
    }

    private void sendMessage(String deviceId, Map<String, Object> message) {
        WebSocketSession session = deviceSessions.get(deviceId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            } catch (IOException e) {
                log.error("发送消息到设备 {} 失败: {}", deviceId, e.getMessage());
            }
        }
    }

    private String extractDeviceId(WebSocketSession session) {
        String path = session.getUri().getPath();
        Map<String, String> parameters = uriTemplate.match(path);
        return parameters.get("deviceId");
    }

    private void handleHeartbeat(String deviceId, Map<String, Object> data) {
        // 更新设备在线状态
        log.debug("设备 {} 心跳", deviceId);
    }

    private void handleReminderAck(String deviceId, Map<String, Object> data) {
        // 处理提醒确认
        log.info("设备 {} 确认提醒: {}", deviceId, data.get("reminderId"));
    }

    private void handleChatMessage(String deviceId, Map<String, Object> data) {
        // 处理聊天消息
        log.info("设备 {} 发送聊天消息: {}", deviceId, data.get("content"));
    }
}