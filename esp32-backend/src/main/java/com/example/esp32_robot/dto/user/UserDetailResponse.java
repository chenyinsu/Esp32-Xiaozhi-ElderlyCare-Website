package com.example.esp32_robot.dto.user;

import com.example.esp32_robot.entity.User;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class UserDetailResponse {
    private Long id;
    private String username;
    private String name;
    private String phone;
    private User.Role role;
    private User.Gender gender;
    private Integer age;
    private String idCard;
    private String address;
    private String emergencyPhone;
    private String medicalHistory;
    private String healthCondition;
    private Long deviceId;
    private String deviceName;
    private List<UserSummaryResponse> managedElders;
    private List<UserSummaryResponse> emergencyContacts;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}