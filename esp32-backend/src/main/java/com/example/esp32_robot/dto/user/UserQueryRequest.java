package com.example.esp32_robot.dto.user;

import com.example.esp32_robot.entity.User;
import lombok.Data;

@Data
public class UserQueryRequest {
    private String name;
    private String phone;
    private User.Role role;
    private User.Gender gender;
    private Integer minAge;
    private Integer maxAge;
    private Boolean isActive;
    private Boolean hasDevice;
    private Integer page = 1;
    private Integer size = 20;
}