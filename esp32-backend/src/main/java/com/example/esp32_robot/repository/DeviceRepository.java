package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.Device;
import com.example.esp32_robot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    // 根据设备唯一标识查询
    Optional<Device> findByDeviceId(String deviceId);

    // 根据设备名称模糊查询
    List<Device> findByDeviceNameContainingIgnoreCase(String deviceName);

    // 根据设备状态查询
    List<Device> findByStatus(Device.Status status);

    // 根据设备是否在线查询
    List<Device> findByStatusIn(List<Device.Status> statuses);

    // 根据设备是否绑定用户查询
    List<Device> findByUserIsNotNull();
    List<Device> findByUserIsNull();

    // 根据绑定的用户查询设备
    Optional<Device> findByUser(User user);
    Optional<Device> findByUserId(Long userId);

    // 根据是否有摄像头查询
    List<Device> findByHasCameraTrue();

    // 根据电量范围查询
    List<Device> findByBatteryLevelBetween(Integer minBattery, Integer maxBattery);

    // 查询在线设备
    List<Device> findByStatusOrderByLastOnlineTimeDesc(Device.Status status);

    // 查询最后在线时间在某个时间点之后的设备
    List<Device> findByLastOnlineTimeAfter(LocalDateTime time);

    // 查询最后在线时间在某个时间点之前的设备（可能离线）
    List<Device> findByLastOnlineTimeBefore(LocalDateTime time);

    // 根据WiFi信号强度查询
    List<Device> findByWifiStrengthGreaterThan(Integer minStrength);

    // 统计各种状态的设备数量
    long countByStatus(Device.Status status);

    // 统计在线的设备数量
    @Query("SELECT COUNT(d) FROM Device d WHERE d.status = 'ONLINE'")
    long countOnlineDevices();

    // 统计电量低于阈值的设备
    @Query("SELECT COUNT(d) FROM Device d WHERE d.batteryLevel < :threshold")
    long countLowBatteryDevices(@Param("threshold") Integer threshold);

    // 根据地理位置范围查询设备
    @Query("SELECT d FROM Device d WHERE d.latitude BETWEEN :minLat AND :maxLat " +
            "AND d.longitude BETWEEN :minLng AND :maxLng")
    List<Device> findByLocationRange(
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLng") Double minLng,
            @Param("maxLng") Double maxLng
    );

    // 查找最近更新过的设备
    List<Device> findTop10ByOrderByUpdatedAtDesc();

    // 批量更新设备状态
    @Query("UPDATE Device d SET d.status = :status WHERE d.id IN :ids")
    int updateDeviceStatus(@Param("ids") List<Long> ids, @Param("status") Device.Status status);
}