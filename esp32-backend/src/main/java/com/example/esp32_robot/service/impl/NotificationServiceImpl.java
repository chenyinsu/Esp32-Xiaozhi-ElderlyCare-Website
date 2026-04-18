package com.example.esp32_robot.service.impl;

import com.example.esp32_robot.dto.notification.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.converter.DtoConverter;
import com.example.esp32_robot.entity.Notification;
import com.example.esp32_robot.entity.User;
import com.example.esp32_robot.exception.ResourceNotFoundException;
import com.example.esp32_robot.repository.NotificationRepository;
import com.example.esp32_robot.repository.UserRepository;
import com.example.esp32_robot.service.NotificationService;
import com.example.esp32_robot.service.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl extends BaseService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for receiver: {}", request.getReceiverId());

        Notification notification = dtoConverter.toNotificationEntity(request);

        if (request.getSenderId() != null) {
            User sender = userRepository.findById(request.getSenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getSenderId()));
            notification.setSender(sender);
        }

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getReceiverId()));
        notification.setReceiver(receiver);

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Notification created successfully with id: {}", savedNotification.getId());

        return dtoConverter.toNotificationResponse(savedNotification);
    }

    @Override
    public NotificationResponse createNotification(Notification notification) {
        log.info("Creating notification from entity for receiver: {}",
                notification.getReceiver() != null ? notification.getReceiver().getId() : "unknown");

        Notification savedNotification = notificationRepository.save(notification);
        return dtoConverter.toNotificationResponse(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(Long id) {
        log.info("Fetching notification with id: {}", id);

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        return dtoConverter.toNotificationResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> queryNotifications(NotificationQueryRequest request) {
        log.info("Querying notifications with filters");

        Pageable pageable = createPageable(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage = notificationRepository.findAll(pageable);

        List<NotificationResponse> filteredResponses = filterAndConvertNotifications(
                notificationPage.getContent(), request);

        return PageResponse.of(filteredResponses, request.getPage(), request.getSize(),
                (long) filteredResponses.size());
    }

    private List<NotificationResponse> filterAndConvertNotifications(List<Notification> notifications,
                                                                     NotificationQueryRequest request) {
        return notifications.stream()
                .filter(n -> filterByReceiverId(n, request.getReceiverId()))
                .filter(n -> filterBySenderId(n, request.getSenderId()))
                .filter(n -> filterByIsRead(n, request.getIsRead()))
                .filter(n -> filterByType(n, request.getType()))
                .filter(n -> filterByLevel(n, request.getLevel()))
                .filter(n -> filterByRelatedId(n, request))
                .filter(n -> filterByTimeRange(n, request.getStartTime(), request.getEndTime()))
                .filter(n -> filterByExpired(n, request.getIncludeExpired()))
                .map(dtoConverter::toNotificationResponse)
                .toList();
    }

    private boolean filterByReceiverId(Notification notification, Long receiverId) {
        return receiverId == null || notification.getReceiver().getId().equals(receiverId);
    }

    private boolean filterBySenderId(Notification notification, Long senderId) {
        return senderId == null ||
                (notification.getSender() != null && notification.getSender().getId().equals(senderId));
    }

    private boolean filterByIsRead(Notification notification, Boolean isRead) {
        return isRead == null || notification.getIsRead().equals(isRead);
    }

    private boolean filterByType(Notification notification, Notification.NotificationType type) {
        return type == null || notification.getType() == type;
    }

    private boolean filterByLevel(Notification notification, Notification.NotificationLevel level) {
        return level == null || notification.getLevel() == level;
    }

    private boolean filterByRelatedId(Notification notification, NotificationQueryRequest request) {
        if (request.getRelatedEmergencyId() != null &&
                !request.getRelatedEmergencyId().equals(notification.getRelatedEmergencyId())) {
            return false;
        }
        if (request.getRelatedReminderId() != null &&
                !request.getRelatedReminderId().equals(notification.getRelatedReminderId())) {
            return false;
        }
        if (request.getRelatedReportId() != null &&
                !request.getRelatedReportId().equals(notification.getRelatedReportId())) {
            return false;
        }
        return true;
    }

    private boolean filterByTimeRange(Notification notification, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && notification.getCreatedAt().isBefore(startTime)) return false;
        if (endTime != null && notification.getCreatedAt().isAfter(endTime)) return false;
        return true;
    }

    private boolean filterByExpired(Notification notification, Boolean includeExpired) {
        if (Boolean.TRUE.equals(includeExpired)) return true;
        return !notification.isExpired();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotificationsByReceiver(Long receiverId) {
        log.info("Fetching notifications for receiver: {}", receiverId);

        List<Notification> notifications = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
        return dtoConverter.toNotificationResponseList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long receiverId) {
        log.info("Fetching unread notifications for receiver: {}", receiverId);

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", receiverId));

        List<Notification> notifications = notificationRepository
                .findByReceiverAndIsReadOrderByCreatedAtDesc(receiver, false);
        return dtoConverter.toNotificationResponseList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUrgentUnreadNotifications() {
        log.info("Fetching urgent unread notifications");

        List<Notification> notifications = notificationRepository.findUrgentUnreadNotifications();
        return dtoConverter.toNotificationResponseList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationStatisticsResponse getNotificationStatistics(Long receiverId) {
        log.info("Calculating notification statistics for receiver: {}", receiverId);

        List<Notification> notifications = notificationRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);

        long totalNotifications = notifications.size();
        long unreadCount = notifications.stream().filter(n -> !n.getIsRead()).count();
        long readCount = totalNotifications - unreadCount;
        long urgentCount = notifications.stream()
                .filter(n -> n.getLevel() == Notification.NotificationLevel.DANGER ||
                        n.getLevel() == Notification.NotificationLevel.CRITICAL)
                .count();
        long reminderCount = notifications.stream()
                .filter(n -> n.getType() == Notification.NotificationType.REMINDER).count();
        long emergencyCount = notifications.stream()
                .filter(n -> n.getType() == Notification.NotificationType.EMERGENCY).count();
        long reportCount = notifications.stream()
                .filter(n -> n.getType() == Notification.NotificationType.REPORT).count();
        long systemCount = notifications.stream()
                .filter(n -> n.getType() == Notification.NotificationType.SYSTEM).count();

        return NotificationStatisticsResponse.builder()
                .totalNotifications(totalNotifications)
                .unreadCount(unreadCount)
                .readCount(readCount)
                .urgentCount(urgentCount)
                .reminderCount(reminderCount)
                .emergencyCount(emergencyCount)
                .reportCount(reportCount)
                .systemCount(systemCount)
                .build();
    }

    @Override
    public void markAsRead(List<Long> ids) {
        log.info("Marking {} notifications as read", ids.size());

        notificationRepository.markAsRead(ids, LocalDateTime.now());
        log.info("Notifications marked as read successfully");
    }

    @Override
    public void markAllAsRead(Long receiverId) {
        log.info("Marking all notifications as read for receiver: {}", receiverId);

        notificationRepository.markAllAsRead(receiverId, LocalDateTime.now());
        log.info("All notifications marked as read successfully");
    }

    @Override
    public void deleteNotification(Long id) {
        log.info("Deleting notification with id: {}", id);

        if (!notificationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Notification", "id", id);
        }

        notificationRepository.deleteById(id);
        log.info("Notification deleted successfully");
    }

    @Override
    public void deleteExpiredNotifications() {
        log.info("Deleting expired notifications");

        int deletedCount = notificationRepository.deleteExpiredNotifications();
        log.info("Deleted {} expired notifications", deletedCount);
    }

    /**
     * 定时任务：每天凌晨2点清理过期通知
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void scheduledDeleteExpiredNotifications() {
        log.info("Running scheduled expired notification cleanup");
        deleteExpiredNotifications();
    }
}