package com.example.esp32_robot.service;

import com.example.esp32_robot.dto.user.*;
import com.example.esp32_robot.dto.common.PageResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse updateUser(Long id, UserRequest request);

    UserDetailResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    UserResponse getUserByPhone(String phone);

    PageResponse<UserResponse> queryUsers(UserQueryRequest request);

    List<UserResponse> getUsersByRole(User.Role role);

    List<UserResponse> getEldersWithoutDevice();

    LoginResponse login(LoginRequest request);

    void changePassword(Long userId, PasswordChangeRequest request);

    UserStatisticsResponse getUserStatistics();

    void deleteUser(Long id);

    void batchUpdateUserStatus(List<Long> ids, Boolean isActive);
}