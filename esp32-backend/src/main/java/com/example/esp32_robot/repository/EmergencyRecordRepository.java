package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.Emergency;
import com.example.esp32_robot.entity.EmergencyRecord;
import com.example.esp32_robot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EmergencyRecordRepository extends JpaRepository<EmergencyRecord, Long> {

    // 根据紧急事件查询处理记录
    List<EmergencyRecord> findByEmergencyOrderByActionTimeDesc(Emergency emergency);
    List<EmergencyRecord> findByEmergencyIdOrderByActionTimeDesc(Long emergencyId);

    // 根据处理人查询
    List<EmergencyRecord> findByUserOrderByActionTimeDesc(User user);
    List<EmergencyRecord> findByUserIdOrderByActionTimeDesc(Long userId);

    // 根据处理动作类型查询
    List<EmergencyRecord> findByActionTypeOrderByActionTimeDesc(EmergencyRecord.ActionType actionType);

    // 根据时间段查询
    List<EmergencyRecord> findByActionTimeBetweenOrderByActionTimeDesc(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // 根据紧急事件和处理人查询
    List<EmergencyRecord> findByEmergencyAndUserOrderByActionTimeDesc(
            Emergency emergency,
            User user
    );

    // 统计处理记录数量
    long countByEmergency(Emergency emergency);
    long countByUserId(Long userId);

    // 获取紧急事件的最后一条处理记录
    @Query("SELECT er FROM EmergencyRecord er WHERE er.emergency = :emergency " +
            "ORDER BY er.actionTime DESC LIMIT 1")
    EmergencyRecord findLatestByEmergency(@Param("emergency") Emergency emergency);

    // 获取处理人最近的处理记录
    List<EmergencyRecord> findTop10ByUserOrderByActionTimeDesc(User user);

    // 统计各种处理动作的数量
    @Query("SELECT er.actionType, COUNT(er) FROM EmergencyRecord er " +
            "WHERE er.actionTime >= :startTime " +
            "GROUP BY er.actionType")
    List<Object[]> countByActionTypeSince(@Param("startTime") LocalDateTime startTime);

    // 获取紧急事件的完整处理流程
    @Query("SELECT er FROM EmergencyRecord er WHERE er.emergency.id = :emergencyId " +
            "ORDER BY er.actionTime ASC")
    List<EmergencyRecord> getEmergencyProcess(@Param("emergencyId") Long emergencyId);
}