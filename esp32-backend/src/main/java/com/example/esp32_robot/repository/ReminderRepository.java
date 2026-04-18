package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    List<Reminder> findByDeviceIdOrderByRemindTimeAsc(Long deviceId);

    List<Reminder> findByUserIdOrderByRemindTimeAsc(Long userId);

    @Query("SELECT r FROM Reminder r WHERE r.isActive = true AND r.nextTrigger <= :currentTime")
    List<Reminder> findRemindersToTrigger(@Param("currentTime") LocalDateTime currentTime);

    List<Reminder> findByIsActiveTrueOrderByRemindTimeAsc();

    @Query("SELECT r FROM Reminder r WHERE r.isActive = true")
    List<Reminder> findAllActiveReminders();

    long countByReminderType(Reminder.ReminderType type);

    long countByIsActive(Boolean isActive);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(r) FROM Reminder r WHERE r.user.id = :userId AND r.reminderType = 'MEDICATION' " +
            "AND r.isTaken = true AND r.takenTime IS NOT NULL")
    long countMedicationTaken(@Param("userId") Long userId);

    @Query("SELECT COUNT(r) FROM Reminder r WHERE r.user.id = :userId AND r.reminderType = 'MEDICATION' " +
            "AND r.isTaken = false AND r.remindTime < CURRENT_TIME")
    long countMedicationMissed(@Param("userId") Long userId);

    @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.isActive = true " +
            "AND r.remindTime > CURRENT_TIME ORDER BY r.remindTime ASC")
    List<Reminder> findUpcomingReminders(@Param("userId") Long userId);

    @Query("SELECT r FROM Reminder r WHERE r.user.id = :userId AND r.reminderType = 'MEDICATION' " +
            "AND r.takenTime IS NOT NULL ORDER BY r.takenTime DESC")
    List<Reminder> findMedicationHistory(@Param("userId") Long userId);

    @Modifying  // ✅ 添加这个注解
    @Query("UPDATE Reminder r SET r.isActive = :isActive WHERE r.id IN :ids")
    int updateReminderStatus(@Param("ids") List<Long> ids, @Param("isActive") Boolean isActive);
}