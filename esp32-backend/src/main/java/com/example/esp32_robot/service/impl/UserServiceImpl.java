package com.example.esp32_robot.service.impl;

import com.example.esp32_robot.dto.user.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.converter.DtoConverter;
import com.example.esp32_robot.entity.User;
import com.example.esp32_robot.exception.BusinessException;
import com.example.esp32_robot.exception.ResourceNotFoundException;
import com.example.esp32_robot.exception.UnauthorizedException;
import com.example.esp32_robot.repository.UserRepository;
import com.example.esp32_robot.service.UserService;
import com.example.esp32_robot.service.base.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl extends BaseService implements UserService {

    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;

    // 简单的内存令牌存储（生产环境应使用Redis）
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    @Override
    public UserResponse createUser(UserRequest request) {
        log.info("Creating user with username: {}", request.getUsername());

        // 检查用户名是否已存在
        userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
            throw new BusinessException("Username already exists: " + request.getUsername());
        });

        // 检查手机号是否已存在
        userRepository.findByPhone(request.getPhone()).ifPresent(u -> {
            throw new BusinessException("Phone already exists: " + request.getPhone());
        });

        User user = dtoConverter.toUserEntity(request);

        // TODO: 密码加密
        // user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());

        return dtoConverter.toUserResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // 检查用户名是否被其他用户使用
        if (!user.getUsername().equals(request.getUsername())) {
            userRepository.findByUsername(request.getUsername()).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    throw new BusinessException("Username already exists: " + request.getUsername());
                }
            });
        }

        // 检查手机号是否被其他用户使用
        if (!user.getPhone().equals(request.getPhone())) {
            userRepository.findByPhone(request.getPhone()).ifPresent(u -> {
                if (!u.getId().equals(id)) {
                    throw new BusinessException("Phone already exists: " + request.getPhone());
                }
            });
        }

        dtoConverter.updateUserEntity(request, user);

        // 如果密码有变化
        if (StringUtils.hasText(request.getPassword())) {
            // TODO: 密码加密
            user.setPassword(request.getPassword());
        }

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully");

        return dtoConverter.toUserResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long id) {
        log.info("Fetching user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        return dtoConverter.toUserDetailResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        log.info("Fetching user with username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        return dtoConverter.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByPhone(String phone) {
        log.info("Fetching user with phone: {}", phone);

        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", phone));

        return dtoConverter.toUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> queryUsers(UserQueryRequest request) {
        log.info("Querying users with filters");

        Pageable pageable = createPageable(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.ASC, "name"));

        Page<User> userPage = userRepository.findAll(pageable);

        List<UserResponse> filteredResponses = filterAndConvertUsers(userPage.getContent(), request);

        return PageResponse.of(filteredResponses, request.getPage(), request.getSize(),
                (long) filteredResponses.size());
    }

    private List<UserResponse> filterAndConvertUsers(List<User> users, UserQueryRequest request) {
        return users.stream()
                .filter(u -> filterByName(u, request.getName()))
                .filter(u -> filterByPhone(u, request.getPhone()))
                .filter(u -> filterByRole(u, request.getRole()))
                .filter(u -> filterByGender(u, request.getGender()))
                .filter(u -> filterByAge(u, request.getMinAge(), request.getMaxAge()))
                .filter(u -> filterByIsActive(u, request.getIsActive()))
                .filter(u -> filterByHasDevice(u, request.getHasDevice()))
                .map(dtoConverter::toUserResponse)
                .toList();
    }

    private boolean filterByName(User user, String name) {
        return name == null || user.getName().toLowerCase().contains(name.toLowerCase());
    }

    private boolean filterByPhone(User user, String phone) {
        return phone == null || user.getPhone().contains(phone);
    }

    private boolean filterByRole(User user, User.Role role) {
        return role == null || user.getRole() == role;
    }

    private boolean filterByGender(User user, User.Gender gender) {
        return gender == null || user.getGender() == gender;
    }

    private boolean filterByAge(User user, Integer minAge, Integer maxAge) {
        if (user.getAge() == null) return true;
        if (minAge != null && user.getAge() < minAge) return false;
        if (maxAge != null && user.getAge() > maxAge) return false;
        return true;
    }

    private boolean filterByIsActive(User user, Boolean isActive) {
        return isActive == null || user.getIsActive().equals(isActive);
    }

    private boolean filterByHasDevice(User user, Boolean hasDevice) {
        if (hasDevice == null) return true;
        boolean userHasDevice = user.getDeviceId() != null;
        return hasDevice.equals(userHasDevice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(User.Role role) {
        log.info("Fetching users with role: {}", role);

        List<User> users = userRepository.findByRoleOrderByNameAsc(role);
        return dtoConverter.toUserResponseList(users);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getEldersWithoutDevice() {
        log.info("Fetching elders without device");

        List<User> users = userRepository.findEldersWithoutDevice();
        return dtoConverter.toUserResponseList(users);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("User login attempt: {}", request.getUsername());

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!user.getIsActive()) {
            throw new UnauthorizedException("Account is inactive");
        }

        // TODO: 密码验证
        // if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        //     throw new UnauthorizedException("Invalid username or password");
        // }

        if (!request.getPassword().equals(user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        // 生成令牌
        String token = generateToken(user);

        log.info("User logged in successfully: {}", user.getUsername());

        return dtoConverter.toLoginResponse(user, token);
    }

    private String generateToken(User user) {
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user.getId());
        return token;
    }

    @Override
    public void changePassword(Long userId, PasswordChangeRequest request) {
        log.info("Changing password for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 验证旧密码
        if (!request.getOldPassword().equals(user.getPassword())) {
            throw new BusinessException("Invalid old password");
        }

        // 更新密码
        user.setPassword(request.getNewPassword());
        userRepository.save(user);

        log.info("Password changed successfully for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatisticsResponse getStatistics() {
        log.info("Calculating user statistics");

        List<User> allUsers = userRepository.findAll();

        long totalUsers = allUsers.size();
        long elderlyCount = countByRole(allUsers, User.Role.ELDERLY);
        long communityStaffCount = countByRole(allUsers, User.Role.COMMUNITY);
        long staffCount = countByRole(allUsers, User.Role.STAFF);
        long childCount = countByRole(allUsers, User.Role.CHILD);
        long hospitalCount = countByRole(allUsers, User.Role.HOSPITAL);

        long activeUsers = allUsers.stream().filter(User::getIsActive).count();
        long inactiveUsers = totalUsers - activeUsers;
        long usersWithDevice = allUsers.stream().filter(u -> u.getDeviceId() != null).count();

        Map<String, Long> usersByGender = allUsers.stream()
                .filter(u -> u.getGender() != null)
                .collect(Collectors.groupingBy(u -> u.getGender().name(), Collectors.counting()));

        Map<String, Double> averageAgeByRole = allUsers.stream()
                .filter(u -> u.getAge() != null)
                .collect(Collectors.groupingBy(
                        u -> u.getRole().name(),
                        Collectors.averagingInt(User::getAge)
                ));

        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .elderlyCount(elderlyCount)
                .communityStaffCount(communityStaffCount)
                .staffCount(staffCount)
                .childCount(childCount)
                .hospitalCount(hospitalCount)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .usersWithDevice(usersWithDevice)
                .usersByGender(usersByGender)
                .averageAgeByRole(averageAgeByRole)
                .build();
    }

    private long countByRole(List<User> users, User.Role role) {
        return users.stream().filter(u -> u.getRole() == role).count();
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // 检查是否可以删除
        if (user.getRole() == User.Role.ELDERLY && user.getDeviceId() != null) {
            throw new BusinessException("Cannot delete elderly user with bound device");
        }

        userRepository.delete(user);
        log.info("User deleted successfully");
    }

    @Override
    public void batchUpdateUserStatus(List<Long> ids, Boolean isActive) {
        log.info("Batch updating user status for {} users to {}", ids.size(), isActive);

        userRepository.updateUserStatus(ids, isActive);
        log.info("Batch update completed");
    }

    /**
     * 验证令牌
     */
    public Long validateToken(String token) {
        return tokenStore.get(token);
    }

    /**
     * 登出
     */
    public void logout(String token) {
        tokenStore.remove(token);
        log.info("User logged out");
    }
}