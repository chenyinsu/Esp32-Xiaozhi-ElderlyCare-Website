package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 根据用户名查询
    Optional<User> findByUsername(String username);

    // 根据手机号查询
    Optional<User> findByPhone(String phone);

    // 根据身份证号查询
    Optional<User> findByIdCard(String idCard);

    // 根据角色查询
    List<User> findByRoleOrderByNameAsc(User.Role role);

    // 根据年龄范围查询
    List<User> findByAgeBetween(Integer minAge, Integer maxAge);

    // 根据是否激活查询
    List<User> findByIsActiveOrderByNameAsc(Boolean isActive);

    // 根据设备ID查询用户
    Optional<User> findByDeviceId(Long deviceId);

    // 根据姓名模糊查询
    List<User> findByNameContainingIgnoreCase(String name);

    // 根据地址模糊查询
    List<User> findByAddressContainingIgnoreCase(String address);

    // 统计各类用户数量
    long countByRole(User.Role role);
    long countByIsActive(Boolean isActive);

    // 统计有设备的用户数量
    long countByDeviceIdIsNotNull();

    // 获取社区工作人员管理的老年人
    @Query("SELECT u.managedElders FROM User u WHERE u.id = :staffId")
    List<User> findManagedElders(@Param("staffId") Long staffId);

    // 获取用户的紧急联系人
    @Query("SELECT u.emergencyContacts FROM User u WHERE u.id = :userId")
    List<User> findEmergencyContacts(@Param("userId") Long userId);

    // 搜索用户（多条件）
    @Query("SELECT u FROM User u WHERE " +
            "(:name IS NULL OR u.name LIKE %:name%) AND " +
            "(:phone IS NULL OR u.phone LIKE %:phone%) AND " +
            "(:role IS NULL OR u.role = :role) AND " +
            "(:isActive IS NULL OR u.isActive = :isActive)")
    List<User> searchUsers(
            @Param("name") String name,
            @Param("phone") String phone,
            @Param("role") User.Role role,
            @Param("isActive") Boolean isActive
    );

    // 获取用户的统计信息
    @Query("SELECT u.role, COUNT(u), AVG(u.age) FROM User u GROUP BY u.role")
    List<Object[]> getUserStatistics();

    // 批量更新用户状态
    @Query("UPDATE User u SET u.isActive = :isActive WHERE u.id IN :ids")
    int updateUserStatus(@Param("ids") List<Long> ids, @Param("isActive") Boolean isActive);

    // 查找可分配的设备（没有绑定设备的老年人）
    @Query("SELECT u FROM User u WHERE u.role = 'ELDERLY' AND u.deviceId IS NULL")
    List<User> findEldersWithoutDevice();

    // 验证用户登录
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.password = :password AND u.isActive = true")
    Optional<User> authenticate(@Param("username") String username, @Param("password") String password);

    // 获取用户的简单信息（用于列表展示）
    @Query("SELECT u.id, u.name, u.phone, u.role, u.deviceId FROM User u WHERE u.id = :userId")
    Optional<Object[]> getUserSummary(@Param("userId") Long userId);
}