package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.Emergency;
import com.example.esp32_robot.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {

    // 根据处理状态查询
    List<Emergency> findByStatusOrderByTriggerTimeDesc(Emergency.Status status);
    Page<Emergency> findByStatus(Emergency.Status status, Pageable pageable);

    // 根据紧急程度查询
    List<Emergency> findByEmergencyLevelOrderByTriggerTimeDesc(Emergency.EmergencyLevel level);

    // 根据事件类型查询
    List<Emergency> findByEmergencyTypeOrderByTriggerTimeDesc(Emergency.EmergencyType type);

    // 根据关联的设备查询
    List<Emergency> findByDeviceIdOrderByTriggerTimeDesc(Long deviceId);
    List<Emergency> findByDevice_DeviceIdOrderByTriggerTimeDesc(String deviceId);

    // 根据关联的用户（老年人）查询
    List<Emergency> findByUserIdOrderByTriggerTimeDesc(Long userId);

    // 根据处理人查询
    List<Emergency> findByHandledByOrderByTriggerTimeDesc(User handledBy);
    List<Emergency> findByHandledByIdOrderByTriggerTimeDesc(Long handlerId);

    // 根据触发源查询
    List<Emergency> findByTriggerSourceOrderByTriggerTimeDesc(String triggerSource);

    // 根据时间段查询
    List<Emergency> findByTriggerTimeBetweenOrderByTriggerTimeDesc(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // 根据创建时间查询
    List<Emergency> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // 多条件组合查询
    List<Emergency> findByStatusAndEmergencyLevelOrderByTriggerTimeDesc(
            Emergency.Status status,
            Emergency.EmergencyLevel level
    );

    // 统计各种状态的紧急事件数量
    long countByStatus(Emergency.Status status);
    long countByEmergencyLevel(Emergency.EmergencyLevel level);

    // 统计今日紧急事件
    @Query("SELECT COUNT(e) FROM Emergency e WHERE DATE(e.triggerTime) = CURRENT_DATE")
    long countTodayEmergencies();

    // 统计本周紧急事件
    @Query("SELECT COUNT(e) FROM Emergency e WHERE WEEK(e.triggerTime) = WEEK(CURRENT_DATE)")
    long countThisWeekEmergencies();

    // 统计设备产生的紧急事件数量
    long countByDeviceId(Long deviceId);

    // 搜索紧急事件（多条件）
    @Query("SELECT e FROM Emergency e WHERE " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:level IS NULL OR e.emergencyLevel = :level) AND " +
            "(:type IS NULL OR e.emergencyType = :type) AND " +
            "(:deviceId IS NULL OR e.device.id = :deviceId) AND " +
            "(e.triggerTime BETWEEN :startTime AND :endTime)")
    List<Emergency> searchEmergencies(
            @Param("status") Emergency.Status status,
            @Param("level") Emergency.EmergencyLevel level,
            @Param("type") Emergency.EmergencyType type,
            @Param("deviceId") Long deviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    // 获取需要处理的紧急事件（未处理或处理中）
    @Query("SELECT e FROM Emergency e WHERE e.status IN ('PENDING', 'HANDLING') ORDER BY e.triggerTime DESC")
    List<Emergency> findToBeHandledEmergencies();

    // 获取最近24小时的紧急事件
    @Query("SELECT e FROM Emergency e WHERE e.triggerTime > :time ORDER BY e.triggerTime DESC")
    List<Emergency> findRecentEmergencies(@Param("time") LocalDateTime time);

    // 统计每个设备的紧急事件趋势
    @Query("SELECT e.device.deviceId, COUNT(e), e.emergencyType FROM Emergency e " +
            "WHERE e.triggerTime >= :startTime " +
            "GROUP BY e.device.deviceId, e.emergencyType")
    List<Object[]> countEmergenciesByDeviceAndType(
            @Param("startTime") LocalDateTime startTime
    );

    // 获取处理时间最长的紧急事件
    @Query("SELECT e FROM Emergency e WHERE e.status = 'RESOLVED' " +
            "ORDER BY (e.resolvedTime - e.triggerTime) DESC")
    List<Emergency> findLongestResolvedEmergencies(Pageable pageable);
}