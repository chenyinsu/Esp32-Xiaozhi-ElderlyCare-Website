package com.example.esp32_robot.handler; // 包路径要能被主应用类扫描到

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

@Component // 关键：让Spring管理为Bean
public class AlertWebSocketHandler implements WebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 连接建立时的逻辑（例如：前端订阅告警）
        System.out.println("前端告警连接成功: " + session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 处理前端发来的消息（例如：确认告警）
        System.out.println("收到前端消息: " + message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // 传输错误处理
        System.err.println("前端告警连接异常: " + exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus closeStatus) throws Exception {
        // 连接关闭时的逻辑
        System.out.println("前端告警连接关闭: " + session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false; // 不支持部分消息
    }
}