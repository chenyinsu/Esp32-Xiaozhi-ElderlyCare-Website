package com.example.esp32_robot.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.example.esp32_robot.websocket.DeviceWebSocketHandler;
import com.example.esp32_robot.websocket.AlertWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DeviceWebSocketHandler deviceWebSocketHandler;
    private final AlertWebSocketHandler alertWebSocketHandler;

    public WebSocketConfig(DeviceWebSocketHandler deviceWebSocketHandler,
                           AlertWebSocketHandler alertWebSocketHandler) {
        this.deviceWebSocketHandler = deviceWebSocketHandler;
        this.alertWebSocketHandler = alertWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 设备端WebSocket连接
        registry.addHandler(deviceWebSocketHandler, "/ws/device/{deviceId}")
                .setAllowedOrigins("*");

        // 前端告警WebSocket连接
        registry.addHandler(alertWebSocketHandler, "/ws/alerts")
                .setAllowedOrigins("*");
    }
}