package com.example.esp32_robot.service.impl;

import com.example.esp32_robot.dto.emergency.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.converter.DtoConverter;
import com.example.esp32_robot.entity.*;
import com.example.esp32_robot.exception.ResourceNotFoundException;
import com.example.esp32_robot.repository.*;
import com.example.esp32_robot.service.EmergencyService;
import com.example.esp32_robot.service.NotificationService;
import com.example.esp32_robot.service.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmergencyServiceImpl extends BaseService implements EmergencyService {

    private final EmergencyRepository emergencyRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;
    private final NotificationService notificationService;

    @Override
    public EmergencyResponse createEmergency(EmergencyRequest request) {
        log.info("Creating emergency for device: {}, user: {}", request.getDeviceId(), request.getUserId());

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", request.getDeviceId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Emergency emergency = dtoConverter.toEmergencyEntity(request);
        emergency.setDevice(device);
        emergency.setUser(user);
        emergency.setStatus(Emergency.Status.PENDING);

        if (emergency.getTriggerTime() == null) {
            emergency.setTriggerTime(LocalDateTime.now());
        }

        Emergency savedEmergency = emergencyRepository.save(emergency);
        log.info("Emergency created successfully with id: {}", savedEmergency.getId());

        // 创建紧急通知
        createEmergencyNotification(savedEmergency);

        return dtoConverter.toEmergencyResponse(savedEmergency);
    }

    private void createEmergencyNotification(Emergency emergency) {
        try {
            Notification notification = Notification.builder()
                    .title("紧急事件告警")
                    .content(String.format("设备 %s 触发紧急事件：%s",
                            emergency.getDevice().getDeviceName(),
                            emergency.getDescription() != null ? emergency.getDescription() : "请及时处理"))
                    .type(Notification.NotificationType.EMERGENCY)
                    .level(mapEmergencyLevelToNotificationLevel(emergency.getEmergencyLevel()))
                    .receiver(findResponsibleStaff(emergency))
                    .relatedEmergencyId(emergency.getId())
                    .build();

            notificationService.createNotification(notification);
        } catch (Exception e) {
            log.error("Failed to create emergency notification", e);
        }
    }

    private Notification.NotificationLevel mapEmergencyLevelToNotificationLevel(Emergency.EmergencyLevel level) {
        return switch (level) {
            case CRITICAL -> Notification.NotificationLevel.CRITICAL;
            case HIGH -> Notification.NotificationLevel.DANGER;
            case MEDIUM -> Notification.NotificationLevel.WARNING;
            case LOW -> Notification.NotificationLevel.INFO;
        };
    }

    private User findResponsibleStaff(Emergency emergency) {
        // 查找负责该用户的工作人员
        List<User> staff = userRepository.findByRoleOrderByNameAsc(User.Role.COMMUNITY);
        return staff.isEmpty() ? null : staff.get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public EmergencyResponse getEmergencyById(Long id) {
        log.info("Fetching emergency with id: {}", id);

        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency", "id", id));

        return dtoConverter.toEmergencyResponse(emergency);
    }

    @Override
    public EmergencyResponse updateEmergencyStatus(Long id, EmergencyHandleRequest request) {
        log.info("Updating emergency status for id: {} to {}", id, request.getStatus());

        Emergency emergency = emergencyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emergency", "id", id));

        User handler = userRepository.findById(request.getHandledById())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getHandledById()));

        emergency.setStatus(request.getStatus());
        emergency.setHandledBy(handler);

        LocalDateTime now = LocalDateTime.now();

        switch (request.getStatus()) {
            case HANDLING:
                if (emergency.getFirstResponseTime() == null) {
                    emergency.setFirstResponseTime(now);
                }
                break;
            case RESOLVED:
                emergency.setResolvedTime(now);
                emergency.setResolutionNote(request.getResolutionNote());
                break;
            case CLOSED:
                emergency.setClosedTime(now);
                emergency.setResolutionNote(request.getResolutionNote());
                break;
        }

        Emergency updatedEmergency = emergencyRepository.save(emergency);
        log.info("Emergency status updated successfully");

        return dtoConverter.toEmergencyResponse(updatedEmergency);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<EmergencyResponse> queryEmergencies(EmergencyQueryRequest request) {
        log.info("Querying emergencies with filters");

        Pageable pageable = createPageable(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "triggerTime"));

        Page<Emergency> emergencyPage;

        if (hasQueryConditions(request)) {
            List<Emergency> emergencies = emergencyRepository.searchEmergencies(
                    request.getStatus(),
                    request.getEmergencyLevel(),
                    request.getEmergencyType(),
                    request.getDeviceId(),
                    request.getStartTime() != null ? request.getStartTime() : LocalDateTime.now().minusMonths(1),
                    request.getEndTime() != null ? request.getEndTime() : LocalDateTime.now()
            );
            emergencyPage = Page.empty(pageable);
        } else {
            emergencyPage = emergencyRepository.findAll(pageable);
        }

        List<EmergencyResponse> filteredResponses = filterAndConvertEmergencies(
                emergencyPage.getContent(), request);

        return PageResponse.of(filteredResponses, request.getPage(), request.getSize(),
                (long) filteredResponses.size());
    }

    private boolean hasQueryConditions(EmergencyQueryRequest request) {
        return request.getStatus() != null ||
                request.getEmergencyLevel() != null ||
                request.getEmergencyType() != null ||
                request.getDeviceId() != null ||
                request.getUserId() != null;
    }

    private List<EmergencyResponse> filterAndConvertEmergencies(List<Emergency> emergencies,
                                                                EmergencyQueryRequest request) {
        return emergencies.stream()
                .filter(e -> filterByUserId(e, request.getUserId()))
                .filter(e -> filterByHandledById(e, request.getHandledById()))
                .filter(e -> filterByTriggerSource(e, request.getTriggerSource()))
                .map(dtoConverter::toEmergencyResponse)
                .toList();
    }

    private boolean filterByUserId(Emergency emergency, Long userId) {
        return userId == null || emergency.getUser().getId().equals(userId);
    }

    private boolean filterByHandledById(Emergency emergency, Long handledById) {
        return handledById == null ||
                (emergency.getHandledBy() != null && emergency.getHandledBy().getId().equals(handledById));
    }

    private boolean filterByTriggerSource(Emergency emergency, String triggerSource) {
        return triggerSource == null ||
                (emergency.getTriggerSource() != null &&
                        emergency.getTriggerSource().toLowerCase().contains(triggerSource.toLowerCase()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyResponse> getPendingEmergencies() {
        log.info("Fetching pending emergencies");

        List<Emergency> emergencies = emergencyRepository.findByStatusOrderByTriggerTimeDesc(Emergency.Status.PENDING);
        return dtoConverter.toEmergencyResponseList(emergencies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyResponse> getEmergenciesByDeviceId(Long deviceId) {
        log.info("Fetching emergencies for device: {}", deviceId);

        List<Emergency> emergencies = emergencyRepository.findByDeviceIdOrderByTriggerTimeDesc(deviceId);
        return dtoConverter.toEmergencyResponseList(emergencies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyResponse> getEmergenciesByUserId(Long userId) {
        log.info("Fetching emergencies for user: {}", userId);

        List<Emergency> emergencies = emergencyRepository.findByUserIdOrderByTriggerTimeDesc(userId);
        return dtoConverter.toEmergencyResponseList(emergencies);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmergencyResponse> getRecentEmergencies(int hours) {
        log.info("Fetching emergencies from last {} hours", hours);

        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        List<Emergency> emergencies = emergencyRepository.findRecentEmergencies(since);
        return dtoConverter.toEmergencyResponseList(emergencies);
    }

    @Override
    @Transactional(readOnly = true)
    public EmergencyStatisticsResponse getEmergencyStatistics() {
        log.info("Calculating emergency statistics");

        List<Emergency> allEmergencies = emergencyRepository.findAll();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        long totalEmergencies = allEmergencies.size();
        long pendingCount = countByStatus(allEmergencies, Emergency.Status.PENDING);
        long handlingCount = countByStatus(allEmergencies, Emergency.Status.HANDLING);
        long resolvedCount = countByStatus(allEmergencies, Emergency.Status.RESOLVED);
        long closedCount = countByStatus(allEmergencies, Emergency.Status.CLOSED);
        long falseAlarmCount = countByStatus(allEmergencies, Emergency.Status.FALSE_ALARM);

        long todayCount = emergencyRepository.countTodayEmergencies();
        long thisWeekCount = emergencyRepository.countThisWeekEmergencies();

        double avgResponseTime = calculateAverageResponseTime(allEmergencies);

        Map<String, Long> countByType = allEmergencies.stream()
                .collect(Collectors.groupingBy(e -> e.getEmergencyType().name(), Collectors.counting()));

        Map<String, Long> countByLevel = allEmergencies.stream()
                .collect(Collectors.groupingBy(e -> e.getEmergencyLevel().name(), Collectors.counting()));

        return EmergencyStatisticsResponse.builder()
                .totalEmergencies(totalEmergencies)
                .pendingCount(pendingCount)
                .handlingCount(handlingCount)
                .resolvedCount(resolvedCount)
                .closedCount(closedCount)
                .falseAlarmCount(falseAlarmCount)
                .todayCount(todayCount)
                .thisWeekCount(thisWeekCount)
                .averageResponseTime(avgResponseTime)
                .countByType(countByType)
                .countByLevel(countByLevel)
                .build();
    }

    private long countByStatus(List<Emergency> emergencies, Emergency.Status status) {
        return emergencies.stream().filter(e -> e.getStatus() == status).count();
    }

    private double calculateAverageResponseTime(List<Emergency> emergencies) {
        return emergencies.stream()
                .filter(e -> e.getFirstResponseTime() != null && e.getTriggerTime() != null)
                .mapToLong(e -> java.time.Duration.between(e.getTriggerTime(), e.getFirstResponseTime()).toMinutes())
                .average()
                .orElse(0.0);
    }

    @Override
    public void deleteEmergency(Long id) {
        log.info("Deleting emergency with id: {}", id);

        if (!emergencyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Emergency", "id", id);
        }

        emergencyRepository.deleteById(id);
        log.info("Emergency deleted successfully");
    }
}