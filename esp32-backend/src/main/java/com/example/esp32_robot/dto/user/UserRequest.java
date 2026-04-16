package com.example.esp32_robot.dto.user;

import com.example.esp32_robot.entity.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.util.List;

@Data
public class UserRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotNull(message = "角色不能为空")
    private User.Role role;

    private User.Gender gender;

    private Integer age;

    private String idCard;

    private String address;

    private String emergencyPhone;

    private String medicalHistory;

    private String healthCondition;

    private Long deviceId;

    private List<Long> managedElderIds;

    private List<Long> emergencyContactIds;

    private Boolean isActive = true;
}