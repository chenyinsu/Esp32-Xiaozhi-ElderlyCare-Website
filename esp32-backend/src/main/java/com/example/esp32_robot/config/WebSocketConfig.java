package com.example.esp32_robot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private DeviceWebSocketHandler deviceWebSocketHandler;

    @Autowired
    private AlertWebSocketHandler alertWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 设备连接端点（硬件订阅提醒等）
        registry.addHandler(deviceWebSocketHandler, "/ws/device/{deviceId}")
                .setAllowedOrigins("*")
                .withSockJS();

        // 告警推送端点（Web前端订阅）
        registry.addHandler(alertWebSocketHandler, "/ws/alerts")
                .setAllowedOrigins("*")
                .withSockJS();
    }
}