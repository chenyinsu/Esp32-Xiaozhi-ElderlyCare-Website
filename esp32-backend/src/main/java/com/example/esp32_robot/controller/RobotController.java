package com.example.esp32_robot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/robot")
public class RobotController {

    @GetMapping("/status")
    public String getRobotStatus() {
        // 这里可以调用 Service 层获取真实状态，暂时返回模拟数据
        return "Robot is online";
    }

    @PostMapping("/control")
    public String controlRobot(@RequestParam String command) {
        // 这里可以调用 Service 层下发指令，暂时返回模拟响应
        return "Command '" + command + "' sent to robot";
    }
}