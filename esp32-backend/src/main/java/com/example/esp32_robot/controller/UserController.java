package com.example.esp32_robot.controller;

import com.example.esp32_robot.dto.ApiResponse;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.user.*;
import com.example.esp32_robot.entity.User;
import com.example.esp32_robot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、信息管理接口")
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody UserRequest request) {
        log.info("REST request to register user: {}", request.getUsername());
        UserResponse response = userService.createUser(request);
        return ApiResponse.success("注册成功", response);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST request to login: {}", request.getUsername());
        LoginResponse response = userService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        log.info("REST request to logout");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            userService.logout(token);
        }
        return ApiResponse.success("登出成功", null);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public ApiResponse<UserDetailResponse> getCurrentUserProfile(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = userService.validateToken(token);
        log.info("REST request to get current user profile: {}", userId);
        UserDetailResponse response = userService.getUserById(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "根据ID获取用户详情")
    @GetMapping("/{id}")
    public ApiResponse<UserDetailResponse> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        log.info("REST request to get user by id: {}", id);
        UserDetailResponse response = userService.getUserById(id);
        return ApiResponse.success(response);
    }

    @Operation(summary = "根据用户名获取用户")
    @GetMapping("/username/{username}")
    public ApiResponse<UserResponse> getUserByUsername(@Parameter(description = "用户名") @PathVariable String username) {
        log.info("REST request to get user by username: {}", username);
        UserResponse response = userService.getUserByUsername(username);
        return ApiResponse.success(response);
    }

    @Operation(summary = "根据手机号获取用户")
    @GetMapping("/phone/{phone}")
    public ApiResponse<UserResponse> getUserByPhone(@Parameter(description = "手机号") @PathVariable String phone) {
        log.info("REST request to get user by phone: {}", phone);
        UserResponse response = userService.getUserByPhone(phone);
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public ApiResponse<UserResponse> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        log.info("REST request to update user: {}", id);
        UserResponse response = userService.updateUser(id, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "修改密码")
    @PatchMapping("/password")
    public ApiResponse<Void> changePassword(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody PasswordChangeRequest request) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = userService.validateToken(token);
        log.info("REST request to change password for user: {}", userId);
        userService.changePassword(userId, request);
        return ApiResponse.success("密码修改成功", null);
    }

    @Operation(summary = "分页查询用户")
    @PostMapping("/query")
    public ApiResponse<PageResponse<UserResponse>> queryUsers(@RequestBody UserQueryRequest request) {
        log.info("REST request to query users");
        PageResponse<UserResponse> response = userService.queryUsers(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "根据角色获取用户列表")
    @GetMapping("/role/{role}")
    public ApiResponse<List<UserResponse>> getUsersByRole(@Parameter(description = "角色") @PathVariable User.Role role) {
        log.info("REST request to get users by role: {}", role);
        List<UserResponse> responses = userService.getUsersByRole(role);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "获取未绑定设备的老年人")
    @GetMapping("/elders/without-device")
    public ApiResponse<List<UserResponse>> getEldersWithoutDevice() {
        log.info("REST request to get elders without device");
        List<UserResponse> responses = userService.getEldersWithoutDevice();
        return ApiResponse.success(responses);
    }

    @Operation(summary = "获取用户统计信息")
    @GetMapping("/statistics")
    public ApiResponse<UserStatisticsResponse> getUserStatistics() {
        log.info("REST request to get user statistics");
        UserStatisticsResponse response = userService.getUserStatistics();
        return ApiResponse.success(response);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        log.info("REST request to delete user: {}", id);
        userService.deleteUser(id);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "批量更新用户状态")
    @PatchMapping("/batch/status")
    public ApiResponse<Void> batchUpdateUserStatus(
            @Parameter(description = "用户ID列表") @RequestBody List<Long> ids,
            @Parameter(description = "是否激活") @RequestParam Boolean isActive) {
        log.info("REST request to batch update user status for {} users to {}", ids.size(), isActive);
        userService.batchUpdateUserStatus(ids, isActive);
        return ApiResponse.success("批量更新成功", null);
    }
}