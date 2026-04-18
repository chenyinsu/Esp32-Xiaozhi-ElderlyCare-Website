package com.example.esp32_robot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("name", "ESP32 Robot API");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("timestamp", java.time.LocalDateTime.now());
        return response;
    }
}