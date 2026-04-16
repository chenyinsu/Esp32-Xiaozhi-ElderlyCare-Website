package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    // 根据设备ID查询聊天记录
    List<Chat> findByDeviceIdOrderByTimestampDesc(String deviceId);

    // 根据用户ID查询聊天记录
    List<Chat> findByUserIdOrderByTimestampDesc(Long userId);

    // 根据设备和用户查询聊天记录
    List<Chat> findByDeviceIdAndUserIdOrderByTimestampDesc(String deviceId, Long userId);

    // 根据时间段查询聊天记录
    List<Chat> findByTimestampBetweenOrderByTimestampDesc(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // 根据消息类型查询
    List<Chat> findByTypeOrderByTimestampDesc(String type);

    // 根据情绪值范围查询
    List<Chat> findBySentimentBetweenOrderByTimestampDesc(Double minSentiment, Double maxSentiment);

    // 获取设备最近的消息
    @Query("SELECT c FROM Chat c WHERE c.deviceId = :deviceId ORDER BY c.timestamp DESC LIMIT 1")
    Chat findLatestByDeviceId(@Param("deviceId") String deviceId);

    // 统计设备消息数量
    long countByDeviceId(String deviceId);

    // 统计用户消息数量
    long countByUserId(Long userId);

    // 根据关键词搜索（JSON字段搜索）
    @Query("SELECT c FROM Chat c WHERE c.keywordsJson LIKE %:keyword%")
    List<Chat> searchByKeyword(@Param("keyword") String keyword);

    // 获取设备在特定时间的消息统计
    @Query("SELECT COUNT(c), c.type FROM Chat c WHERE c.deviceId = :deviceId AND c.timestamp >= :startTime GROUP BY c.type")
    List<Object[]> countByTypeAndTime(
            @Param("deviceId") String deviceId,
            @Param("startTime") LocalDateTime startTime
    );
}