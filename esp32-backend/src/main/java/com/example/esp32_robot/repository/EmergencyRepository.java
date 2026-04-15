package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmergencyRepository extends JpaRepository<Emergency, Long> {
    List<Emergency> findByDeviceIdOrderByCreatedAtDesc(String deviceId);
    List<Emergency> findByStatus(String status);
    List<Emergency> findByNotified(Boolean notified);
}