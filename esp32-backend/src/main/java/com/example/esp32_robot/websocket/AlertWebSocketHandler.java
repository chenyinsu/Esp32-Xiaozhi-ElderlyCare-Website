package com.example.esp32_robot.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlertWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(AlertWebSocketHandler.class);
    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        logger.info("告警客户端连接成功: {}, 当前连接数: {}", session.getId(), sessions.size());

        // 发送历史告警
        sendHistoryAlerts(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        logger.info("收到告警客户端消息: {}", payload);

        // 处理客户端确认等消息
        AlertAck ack = objectMapper.readValue(payload, AlertAck.class);
        if ("ack".equals(ack.getAction())) {
            logger.info("告警已确认: {}", ack.getAlertId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
        logger.info("告警客户端断开连接: {}, 剩余连接数: {}", session.getId(), sessions.size());
    }

    // 发送新告警
    public void sendAlert(String title, String message, String level) {
        Alert alert = new Alert(title, message, level, System.currentTimeMillis());
        broadcast(alert);
    }

    // 广播告警给所有连接的客户端
    public void broadcast(Alert alert) {
        String jsonAlert = toJson(alert);
        sessions.removeIf(session -> !session.isOpen());

        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonAlert));
                }
            } catch (IOException e) {
                logger.error("发送告警失败: {}", e.getMessage());
            }
        });
    }

    private void sendHistoryAlerts(WebSocketSession session) {
        // 发送最近的告警历史
        List<Alert> history = getAlertHistory();
        String jsonHistory = toJson(history);
        try {
            session.sendMessage(new TextMessage(jsonHistory));
        } catch (IOException e) {
            logger.error("发送历史告警失败: {}", e.getMessage());
        }
    }

    private List<Alert> getAlertHistory() {
        // TODO: 从数据库获取告警历史
        return new ArrayList<>();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{\"error\":\"序列化失败\"}";
        }
    }

    // 告警类
    public static class Alert {
        private String title;
        private String message;
        private String level; // info, warning, error
        private long timestamp;

        public Alert() {}

        public Alert(String title, String message, String level, long timestamp) {
            this.title = title;
            this.message = message;
            this.level = level;
            this.timestamp = timestamp;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    private static class AlertAck {
        private String action;
        private String alertId;

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public String getAlertId() { return alertId; }
        public void setAlertId(String alertId) { this.alertId = alertId; }
    }
}