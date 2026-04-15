package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByDeviceId(String deviceId);
    List<Reminder> findByUserId(Long userId);

    @Query("SELECT r FROM Reminder r WHERE r.enabled = true AND r.nextTriggerTime <= :now")
    List<Reminder> findDueReminders(@Param("now") LocalDateTime now);

    long countByDeviceId(String deviceId);
}