package com.example.esp32_robot.dto.user;

import com.example.esp32_robot.entity.User;
import lombok.Data;

@Data
public class UserSummaryResponse {
    private Long id;
    private String name;
    private String phone;
    private User.Role role;
    private Long deviceId;
}