package com.example.esp32_robot.dto.user;

import com.example.esp32_robot.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String username;
    private String name;
    private String phone;
    private User.Role role;
    private String token;
    private Long deviceId;
}