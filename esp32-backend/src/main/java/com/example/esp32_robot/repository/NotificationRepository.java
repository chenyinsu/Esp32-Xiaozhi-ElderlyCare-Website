package com.example.esp32_robot.repository;

import com.example.esp32_robot.entity.Notification;
import com.example.esp32_robot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 根据接收者查询
    List<Notification> findByReceiverOrderByCreatedAtDesc(User receiver);
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);

    // 根据发送者查询
    List<Notification> findBySenderOrderByCreatedAtDesc(User sender);
    List<Notification> findBySenderIdOrderByCreatedAtDesc(Long senderId);

    // 根据是否已读查询
    List<Notification> findByIsReadOrderByCreatedAtDesc(Boolean isRead);
    List<Notification> findByReceiverAndIsReadOrderByCreatedAtDesc(User receiver, Boolean isRead);

    // 根据通知类型查询
    List<Notification> findByTypeOrderByCreatedAtDesc(Notification.NotificationType type);

    // 根据通知级别查询
    List<Notification> findByLevelOrderByCreatedAtDesc(Notification.NotificationLevel level);

    // 根据关联的业务ID查询
    List<Notification> findByRelatedEmergencyIdOrderByCreatedAtDesc(Long emergencyId);
    List<Notification> findByRelatedReminderIdOrderByCreatedAtDesc(Long reminderId);
    List<Notification> findByRelatedReportIdOrderByCreatedAtDesc(Long reportId);

    // 根据接收者和类型查询
    List<Notification> findByReceiverAndTypeOrderByCreatedAtDesc(
            User receiver,
            Notification.NotificationType type
    );

    // 根据时间段查询
    List<Notification> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    // 获取未过期的通知
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NULL OR n.expiresAt > CURRENT_TIMESTAMP")
    List<Notification> findUnexpiredNotifications();

    // 获取已过期的通知
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= CURRENT_TIMESTAMP")
    List<Notification> findExpiredNotifications();

    // 统计未读通知数量
    long countByReceiverAndIsReadFalse(User receiver);
    long countByReceiverIdAndIsReadFalse(Long receiverId);

    // 统计各种类型的通知数量
    long countByType(Notification.NotificationType type);

    // 批量标记为已读
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readTime = :readTime WHERE n.id IN :ids")
    int markAsRead(@Param("ids") List<Long> ids, @Param("readTime") LocalDateTime readTime);

    // 标记用户所有通知为已读
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true, n.readTime = :readTime WHERE n.receiver.id = :receiverId")
    int markAllAsRead(@Param("receiverId") Long receiverId, @Param("readTime") LocalDateTime readTime);

    // 删除过期通知
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt <= CURRENT_TIMESTAMP")
    int deleteExpiredNotifications();

    // 获取用户的最新通知
    @Query("SELECT n FROM Notification n WHERE n.receiver.id = :receiverId " +
            "ORDER BY n.createdAt DESC LIMIT :limit")
    List<Notification> findLatestNotifications(
            @Param("receiverId") Long receiverId,
            @Param("limit") int limit
    );

    // 获取紧急通知（高优先级）
    @Query("SELECT n FROM Notification n WHERE n.level IN ('DANGER', 'CRITICAL') " +
            "AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUrgentUnreadNotifications();
}