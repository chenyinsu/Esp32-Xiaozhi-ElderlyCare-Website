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
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeviceWebSocketHandler.class);
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String deviceId = getDeviceId(session);
        sessions.put(deviceId, session);
        logger.info("设备连接成功: {}, 当前连接数: {}", deviceId, sessions.size());

        // 发送连接成功消息
        sendMessage(session, "连接成功", "connected");
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String deviceId = getDeviceId(session);
        String payload = message.getPayload();
        logger.info("收到设备 {} 的消息: {}", deviceId, payload);

        // 解析消息
        try {
            Message msg = objectMapper.readValue(payload, Message.class);
            switch (msg.getType()) {
                case "status":
                    handleStatusUpdate(session, msg);
                    break;
                case "command_response":
                    handleCommandResponse(session, msg);
                    break;
                case "sensor_data":
                    handleSensorData(session, msg);
                    break;
                default:
                    logger.warn("未知消息类型: {}", msg.getType());
            }
        } catch (Exception e) {
            logger.error("解析消息失败: {}", e.getMessage());
            sendMessage(session, "消息格式错误", "error");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String deviceId = getDeviceId(session);
        sessions.remove(deviceId);
        logger.info("设备断开连接: {}, 原因: {}, 剩余连接数: {}", deviceId, status.getReason(), sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String deviceId = getDeviceId(session);
        logger.error("设备 {} 传输错误: {}", deviceId, exception.getMessage());
        sessions.remove(deviceId);
    }

    // 发送消息给指定设备
    public void sendToDevice(String deviceId, String message, String type) {
        WebSocketSession session = sessions.get(deviceId);
        if (session != null && session.isOpen()) {
            try {
                sendMessage(session, message, type);
            } catch (IOException e) {
                logger.error("发送消息给设备 {} 失败: {}", deviceId, e.getMessage());
            }
        } else {
            logger.warn("设备 {} 不在线", deviceId);
        }
    }

    // 广播消息给所有设备
    public void broadcast(String message, String type) {
        Message msg = new Message(type, message, System.currentTimeMillis());
        String jsonMsg = toJson(msg);

        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMsg));
                } catch (IOException e) {
                    logger.error("广播消息失败: {}", e.getMessage());
                }
            }
        });
    }

    private void sendMessage(WebSocketSession session, String message, String type) throws IOException {
        Message msg = new Message(type, message, System.currentTimeMillis());
        String jsonMsg = toJson(msg);
        session.sendMessage(new TextMessage(jsonMsg));
    }

    private void handleStatusUpdate(WebSocketSession session, Message msg) {
        logger.info("设备状态更新: {}", msg.getData());
        // 处理状态更新逻辑
    }

    private void handleCommandResponse(WebSocketSession session, Message msg) {
        logger.info("命令响应: {}", msg.getData());
        // 处理命令响应逻辑
    }

    private void handleSensorData(WebSocketSession session, Message msg) {
        logger.info("传感器数据: {}", msg.getData());
        // 处理传感器数据逻辑
    }

    private String getDeviceId(WebSocketSession session) {
        // 从URI参数或session属性中获取设备ID
        String query = session.getUri().getQuery();
        if (query != null && query.contains("deviceId=")) {
            return query.split("deviceId=")[1].split("&")[0];
        }
        return session.getId();
    }

    private String toJson(Message msg) {
        try {
            return objectMapper.writeValueAsString(msg);
        } catch (Exception e) {
            return "{\"type\":\"error\",\"message\":\"序列化失败\"}";
        }
    }

    // 内部消息类
    private static class Message {
        private String type;
        private String data;
        private long timestamp;

        public Message() {}

        public Message(String type, String data, long timestamp) {
            this.type = type;
            this.data = data;
            this.timestamp = timestamp;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}