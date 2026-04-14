package com.example.esp32_robot.repository;

import com.example.esp32_robot.model.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, Long> {

    // 按设备ID查询最新状态
    @Query(value = "SELECT * FROM device_status WHERE device_id = :deviceId ORDER BY report_time DESC LIMIT 1", nativeQuery = true)
    DeviceStatus findLatestStatus(@Param("deviceId") String deviceId);

    // 查询设备历史状态
    List<DeviceStatus> findByDeviceIdOrderByReportTimeDesc(String deviceId);

    // 查询最近N小时的设备状态
    @Query("SELECT d FROM DeviceStatus d WHERE d.reportTime >= :startTime AND d.deviceId = :deviceId ORDER BY d.reportTime ASC")
    List<DeviceStatus> findStatusByTimeRange(@Param("deviceId") String deviceId, @Param("startTime") LocalDateTime startTime);
}