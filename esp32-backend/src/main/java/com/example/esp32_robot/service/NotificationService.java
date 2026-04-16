package com.example.esp32_robot.service;

import com.example.esp32_robot.dto.notification.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.entity.Notification;

import java.util.List;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    NotificationResponse createNotification(Notification notification);

    NotificationResponse getNotificationById(Long id);

    PageResponse<NotificationResponse> queryNotifications(NotificationQueryRequest request);

    List<NotificationResponse> getNotificationsByReceiver(Long receiverId);

    List<NotificationResponse> getUnreadNotifications(Long receiverId);

    List<NotificationResponse> getUrgentUnreadNotifications();

    NotificationStatisticsResponse getNotificationStatistics(Long receiverId);

    void markAsRead(List<Long> ids);

    void markAllAsRead(Long receiverId);

    void deleteNotification(Long id);

    void deleteExpiredNotifications();
}