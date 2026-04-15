package com.example.esp32_robot.websocket;

import org.springframework.web.socket.*;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeviceWebSocketHandler implements WebSocketHandler {

    private static final Map<String, WebSocketSession> deviceSessions =
            new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String deviceId = extractDeviceId(session);
        deviceSessions.put(deviceId, session);
        System.out.println("设备 " + deviceId + " 已连接");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
            throws Exception {
        // 处理来自设备的消息
        String payload = (String) message.getPayload();
        Map<String, Object> data = objectMapper.readValue(payload, Map.class);

        String event = (String) data.get("event");
        if ("reminder_ack".equals(event)) {
            System.out.println("设备已确认接收提醒: " + data);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus closeStatus) throws Exception {
        String deviceId = extractDeviceId(session);
        deviceSessions.remove(deviceId);
        System.out.println("设备 " + deviceId + " 已断开连接");
    }

    public void sendToDevice(String deviceId, Map<String, Object> message)
            throws Exception {
        WebSocketSession session = deviceSessions.get(deviceId);
        if (session != null && session.isOpen()) {
            String payload = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(payload));
        } else {
            System.err.println("设备 " + deviceId + " 未连接");
        }
    }

    private String extractDeviceId(WebSocketSession session) {
        // 从URL中提取设备ID
        String uri = session.getUri().toString();
        return uri.substring(uri.lastIndexOf("/") + 1);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        exception.printStackTrace();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}