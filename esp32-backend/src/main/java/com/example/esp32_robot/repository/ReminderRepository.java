package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.Reminder;
import com.example.esp32_robot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    // 根据设备查询提醒
    List<Reminder> findByDeviceIdOrderByRemindTimeAsc(Long deviceId);

    // 根据用户查询提醒
    List<Reminder> findByUserIdOrderByRemindTimeAsc(Long userId);

    // 根据创建者查询提醒
    List<Reminder> findByCreatedByOrderByCreatedAtDesc(Long createdBy);

    // 根据提醒类型查询
    List<Reminder> findByReminderTypeOrderByRemindTimeAsc(Reminder.ReminderType type);

    // 根据是否激活查询
    List<Reminder> findByIsActiveOrderByRemindTimeAsc(Boolean isActive);

    // 根据是否已服用查询（用药提醒）
    List<Reminder> findByIsTakenOrderByTakenTimeDesc(Boolean isTaken);

    // 根据药品名称查询
    List<Reminder> findByMedicationNameContainingIgnoreCaseOrderByRemindTimeAsc(String medicationName);

    // 根据重复类型查询
    List<Reminder> findByRepeatTypeOrderByRemindTimeAsc(Reminder.RepeatType repeatType);

    // 查询特定时间的提醒
    List<Reminder> findByRemindTime(LocalTime remindTime);

    // 查询需要触发的提醒
    @Query("SELECT r FROM Reminder r WHERE r.isActive = true AND r.nextTrigger <= :currentTime")
    List<Reminder> findRemindersToTrigger(@Param("currentTime") LocalDateTime currentTime);

    // 查询今天的提醒
    @Query("SELECT r FROM Reminder r WHERE r.isActive = true AND " +
            "((r.repeatType = 'DAILY') OR " +
            "(r.repeatType = 'WEEKLY' AND FUNCTION('DAYOFWEEK', CURRENT_DATE) IN " +
            "(CASE WHEN 'MONDAY' IN r.repeatDays THEN 2 " +
            "WHEN 'TUESDAY' IN r.repeatDays THEN 3 " +
            "WHEN 'WEDNESDAY' IN r.repeatDays THEN 4 " +
            "WHEN 'THURSDAY' IN r.repeatDays THEN 5 " +
            "WHEN 'FRIDAY' IN r.repeatDays THEN 6 " +
            "WHEN 'SATURDAY' IN r.repeatDays THEN 7 " +
            "WHEN 'SUNDAY' IN r.repeatDays THEN 1 END)) OR " +
            "(r.repeatType = 'WORKDAYS' AND FUNCTION('DAYOFWEEK', CURRENT_DATE) BETWEEN 2 AND 6) OR " +
            "(r.repeatType = 'WEEKENDS' AND FUNCTION('DAYOFWEEK', CURRENT_DATE) IN (1, 7)))")
    List<Reminder> findTodayReminders();

    // 统计各类提醒数量
    long countByReminderType(Reminder.ReminderType type);
    long countByIsActive(Boolean isActive);

    // 统计用户的提醒数量
    long countByUserId(Long userId);

    // 统计按时服药次数
    @Query("SELECT COUNT(r) FROM Reminder r WHERE r.user.id = :userId AND r.reminderType = 'MEDICATION' " +
            "AND r.isTaken = true AND r.takenTime IS NOT NULL")
    long countMedicationTaken(@Param("userId") Long userId);

    // 统计未按时服药次数
    @Query("SELECT COUNT(r) FROM Reminder r WHERE r.user.id = :userId AND r.reminderType = 'MEDICATION' " +
            "AND r.isTaken = false AND r.remindTime < CURRENT_TIME")
    long countMedicationMissed(@Param("userId") Long userId);

    // 获取用户即将到来的提醒
    @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.isActive = true " +
            "AND r.remindTime > CURRENT_TIME ORDER BY r.remindTime ASC LIMIT 5")
    List<Reminder> findUpcomingReminders(@Param("userId") Long userId);

    // 获取用户的用药历史
    @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.reminderType = 'MEDICATION' " +
            "AND r.takenTime IS NOT NULL ORDER BY r.takenTime DESC")
    List<Reminder> findMedicationHistory(@Param("userId") Long userId);

    // 批量更新提醒状态
    @Query("UPDATE Reminder r SET r.isActive = :isActive WHERE r.id IN :ids")
    int updateReminderStatus(@Param("ids") List<Long> ids, @Param("isActive") Boolean isActive);
}