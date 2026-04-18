package com.example.esp32_robot.service.impl;

import com.example.esp32_robot.dto.reminder.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.converter.DtoConverter;
import com.example.esp32_robot.entity.*;
import com.example.esp32_robot.exception.BusinessException;
import com.example.esp32_robot.exception.ResourceNotFoundException;
import com.example.esp32_robot.repository.*;
import com.example.esp32_robot.service.ReminderService;
import com.example.esp32_robot.service.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReminderServiceImpl extends BaseService implements ReminderService {

    private final ReminderRepository reminderRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;

    // ==================== 基础 CRUD 方法 ====================

    @Override
    public ReminderResponse createReminder(ReminderRequest request) {
        log.info("Creating reminder for user: {}", request.getUserId());

        Device device = deviceRepository.findById(request.getDeviceId())
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", request.getDeviceId()));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        Reminder reminder = dtoConverter.toReminderEntity(request);
        reminder.setDevice(device);
        reminder.setUser(user);

        // 计算下次触发时间
        calculateNextTriggerTime(reminder);

        Reminder savedReminder = reminderRepository.save(reminder);
        log.info("Reminder created successfully with id: {}", savedReminder.getId());

        return dtoConverter.toReminderResponse(savedReminder);
    }

    @Override
    public ReminderResponse updateReminder(Long id, ReminderRequest request) {
        log.info("Updating reminder with id: {}", id);

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", "id", id));

        dtoConverter.updateReminderEntity(request, reminder);

        // 重新计算下次触发时间
        calculateNextTriggerTime(reminder);

        Reminder updatedReminder = reminderRepository.save(reminder);
        log.info("Reminder updated successfully");

        return dtoConverter.toReminderResponse(updatedReminder);
    }

    @Override
    @Transactional(readOnly = true)
    public ReminderResponse getReminderById(Long id) {
        log.info("Fetching reminder with id: {}", id);

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", "id", id));

        return dtoConverter.toReminderResponse(reminder);
    }

    @Override
    public void deleteReminder(Long id) {
        log.info("Deleting reminder with id: {}", id);

        if (!reminderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reminder", "id", id);
        }

        reminderRepository.deleteById(id);
        log.info("Reminder deleted successfully");
    }

    // ==================== 查询方法 ====================

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReminderResponse> queryReminders(ReminderQueryRequest request) {
        log.info("Querying reminders with filters");

        List<Reminder> allReminders = reminderRepository.findAll();
        List<ReminderResponse> filteredResponses = filterAndConvertReminders(allReminders, request);

        int start = (request.getPage() - 1) * request.getSize();
        int end = Math.min(start + request.getSize(), filteredResponses.size());

        List<ReminderResponse> pagedContent = start < filteredResponses.size()
                ? filteredResponses.subList(start, end)
                : List.of();

        return PageResponse.of(pagedContent, request.getPage(), request.getSize(),
                (long) filteredResponses.size());
    }

    private List<ReminderResponse> filterAndConvertReminders(List<Reminder> reminders,
                                                             ReminderQueryRequest request) {
        return reminders.stream()
                .filter(r -> filterByDeviceId(r, request.getDeviceId()))
                .filter(r -> filterByUserId(r, request.getUserId()))
                .filter(r -> filterByReminderType(r, request.getReminderType()))
                .filter(r -> filterByRepeatType(r, request.getRepeatType()))
                .filter(r -> filterByIsActive(r, request.getIsActive()))
                .filter(r -> filterByIsTaken(r, request.getIsTaken()))
                .filter(r -> filterByMedicationName(r, request.getMedicationName()))
                .map(dtoConverter::toReminderResponse)
                .collect(Collectors.toList());
    }

    private boolean filterByDeviceId(Reminder reminder, Long deviceId) {
        return deviceId == null || reminder.getDevice().getId().equals(deviceId);
    }

    private boolean filterByUserId(Reminder reminder, Long userId) {
        return userId == null || reminder.getUser().getId().equals(userId);
    }

    private boolean filterByReminderType(Reminder reminder, Reminder.ReminderType type) {
        return type == null || reminder.getReminderType() == type;
    }

    private boolean filterByRepeatType(Reminder reminder, Reminder.RepeatType repeatType) {
        return repeatType == null || reminder.getRepeatType() == repeatType;
    }

    private boolean filterByIsActive(Reminder reminder, Boolean isActive) {
        return isActive == null || reminder.getIsActive().equals(isActive);
    }

    private boolean filterByIsTaken(Reminder reminder, Boolean isTaken) {
        return isTaken == null || reminder.getReminderType() != Reminder.ReminderType.MEDICATION
                || reminder.getIsTaken().equals(isTaken);
    }

    private boolean filterByMedicationName(Reminder reminder, String medicationName) {
        return medicationName == null || (reminder.getMedicationName() != null &&
                reminder.getMedicationName().toLowerCase().contains(medicationName.toLowerCase()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> getRemindersByDeviceId(Long deviceId) {
        log.info("Fetching reminders for device: {}", deviceId);

        List<Reminder> reminders = reminderRepository.findByDeviceIdOrderByRemindTimeAsc(deviceId);
        return dtoConverter.toReminderResponseList(reminders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> getRemindersByUserId(Long userId) {
        log.info("Fetching reminders for user: {}", userId);

        List<Reminder> reminders = reminderRepository.findByUserIdOrderByRemindTimeAsc(userId);
        return dtoConverter.toReminderResponseList(reminders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> getTodayReminders() {
        log.info("Fetching today's reminders");

        // 获取所有激活的提醒
        List<Reminder> activeReminders = reminderRepository.findByIsActiveTrueOrderByRemindTimeAsc();

        // 在内存中过滤今天的提醒
        LocalDate today = LocalDate.now();
        java.time.DayOfWeek todayDayOfWeek = today.getDayOfWeek();

        List<Reminder> todayReminders = activeReminders.stream()
                .filter(r -> isReminderForToday(r, today, todayDayOfWeek))
                .collect(Collectors.toList());

        log.info("Found {} reminders for today", todayReminders.size());

        return dtoConverter.toReminderResponseList(todayReminders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> getRemindersToTrigger() {
        log.info("Fetching reminders to trigger");

        List<Reminder> reminders = reminderRepository.findRemindersToTrigger(LocalDateTime.now());
        return dtoConverter.toReminderResponseList(reminders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reminder> getDueReminders() {
        log.info("Fetching due reminders");

        LocalDateTime now = LocalDateTime.now();

        List<Reminder> dueReminders = reminderRepository.findAll().stream()
                .filter(Reminder::getIsActive)
                .filter(r -> r.getNextTrigger() != null)
                .filter(r -> r.getNextTrigger().isBefore(now) || r.getNextTrigger().isEqual(now))
                .collect(Collectors.toList());

        log.info("Found {} due reminders", dueReminders.size());
        return dueReminders;
    }

    // ==================== 业务方法 ====================

    @Override
    public void triggerReminder(Long reminderId) {
        log.info("Triggering reminder: {}", reminderId);

        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", "id", reminderId));

        if (!reminder.getIsActive()) {
            log.warn("Reminder {} is not active, skipping", reminderId);
            return;
        }

        // 更新最后触发时间
        reminder.setLastTriggered(LocalDateTime.now());

        // 计算下次触发时间
        calculateNextTriggerTime(reminder);

        reminderRepository.save(reminder);

        log.info("Reminder triggered successfully: {} (next trigger: {})",
                reminder.getTitle(), reminder.getNextTrigger());
    }

    @Override
    public ReminderResponse markReminderAsTaken(Long id) {
        log.info("Marking reminder as taken: {}", id);

        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", "id", id));

        if (reminder.getReminderType() != Reminder.ReminderType.MEDICATION) {
            throw new BusinessException("Only medication reminders can be marked as taken");
        }

        reminder.setIsTaken(true);
        reminder.setTakenTime(LocalDateTime.now());

        Reminder savedReminder = reminderRepository.save(reminder);
        log.info("Reminder marked as taken successfully");

        return dtoConverter.toReminderResponse(savedReminder);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicationTrackingResponse getMedicationTracking(Long userId) {
        log.info("Getting medication tracking for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<Reminder> medicationReminders = reminderRepository.findByUserIdOrderByRemindTimeAsc(userId)
                .stream()
                .filter(r -> r.getReminderType() == Reminder.ReminderType.MEDICATION)
                .collect(Collectors.toList());

        long totalReminders = medicationReminders.size();
        long takenCount = medicationReminders.stream().filter(Reminder::getIsTaken).count();
        long missedCount = totalReminders - takenCount;
        double adherenceRate = totalReminders > 0 ? (double) takenCount / totalReminders * 100 : 0.0;

        Map<LocalDate, Boolean> dailyAdherence = medicationReminders.stream()
                .filter(r -> r.getTakenTime() != null)
                .collect(Collectors.toMap(
                        r -> r.getTakenTime().toLocalDate(),
                        Reminder::getIsTaken,
                        (existing, replacement) -> existing
                ));

        return MedicationTrackingResponse.builder()
                .userId(userId)
                .userName(user.getName())
                .totalReminders(totalReminders)
                .takenCount(takenCount)
                .missedCount(missedCount)
                .adherenceRate(adherenceRate)
                .dailyAdherence(dailyAdherence)
                .build();
    }

    @Override
    public void batchUpdateReminderStatus(List<Long> ids, Boolean isActive) {
        log.info("Batch updating reminder status for {} reminders to {}", ids.size(), isActive);

        reminderRepository.updateReminderStatus(ids, isActive);
        log.info("Batch update completed");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 判断提醒是否在今天需要触发
     */
    private boolean isReminderForToday(Reminder reminder, LocalDate today, java.time.DayOfWeek todayDayOfWeek) {
        if (reminder.getRepeatType() == null) {
            return false;
        }

        switch (reminder.getRepeatType()) {
            case DAILY:
                return true;

            case WEEKLY:
                if (reminder.getRepeatDays() == null || reminder.getRepeatDays().isEmpty()) {
                    return false;
                }
                Reminder.DayOfWeek reminderDay = convertToReminderDay(todayDayOfWeek);
                return reminder.getRepeatDays().contains(reminderDay);

            case WORKDAYS:
                return todayDayOfWeek != java.time.DayOfWeek.SATURDAY &&
                        todayDayOfWeek != java.time.DayOfWeek.SUNDAY;

            case WEEKENDS:
                return todayDayOfWeek == java.time.DayOfWeek.SATURDAY ||
                        todayDayOfWeek == java.time.DayOfWeek.SUNDAY;

            case MONTHLY:
                if (reminder.getStartDate() != null) {
                    try {
                        LocalDate startDate = LocalDate.parse(reminder.getStartDate());
                        return today.getDayOfMonth() == startDate.getDayOfMonth();
                    } catch (Exception e) {
                        return false;
                    }
                }
                return false;

            case NONE:
                if (reminder.getStartDate() != null && reminder.getEndDate() != null) {
                    try {
                        LocalDate startDate = LocalDate.parse(reminder.getStartDate());
                        LocalDate endDate = LocalDate.parse(reminder.getEndDate());
                        return !today.isBefore(startDate) && !today.isAfter(endDate);
                    } catch (Exception e) {
                        return false;
                    }
                }
                return reminder.getLastTriggered() == null;

            default:
                return false;
        }
    }

    /**
     * 将 java.time.DayOfWeek 转换为 Reminder.DayOfWeek
     */
    private Reminder.DayOfWeek convertToReminderDay(java.time.DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> Reminder.DayOfWeek.MONDAY;
            case TUESDAY -> Reminder.DayOfWeek.TUESDAY;
            case WEDNESDAY -> Reminder.DayOfWeek.WEDNESDAY;
            case THURSDAY -> Reminder.DayOfWeek.THURSDAY;
            case FRIDAY -> Reminder.DayOfWeek.FRIDAY;
            case SATURDAY -> Reminder.DayOfWeek.SATURDAY;
            case SUNDAY -> Reminder.DayOfWeek.SUNDAY;
        };
    }

    /**
     * 计算下次触发时间
     */
    private void calculateNextTriggerTime(Reminder reminder) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextTrigger = LocalDateTime.of(now.toLocalDate(), reminder.getRemindTime());

        if (nextTrigger.isBefore(now)) {
            nextTrigger = nextTrigger.plusDays(1);
        }

        switch (reminder.getRepeatType()) {
            case DAILY:
                // 每天，保持当前计算
                break;
            case WEEKLY:
                // 每周特定几天
                if (reminder.getRepeatDays() != null && !reminder.getRepeatDays().isEmpty()) {
                    nextTrigger = calculateNextWeeklyTrigger(reminder, nextTrigger);
                }
                break;
            case WORKDAYS:
                // 工作日
                while (isWeekend(nextTrigger.toLocalDate())) {
                    nextTrigger = nextTrigger.plusDays(1);
                }
                break;
            case WEEKENDS:
                // 周末
                while (!isWeekend(nextTrigger.toLocalDate())) {
                    nextTrigger = nextTrigger.plusDays(1);
                }
                break;
            case NONE:
            default:
                // 不重复，如果已经触发过，则设为 null
                if (reminder.getLastTriggered() != null) {
                    nextTrigger = null;
                }
                break;
        }

        reminder.setNextTrigger(nextTrigger);
    }

    /**
     * 计算每周提醒的下次触发时间
     */
    private LocalDateTime calculateNextWeeklyTrigger(Reminder reminder, LocalDateTime baseTime) {
        LocalDateTime nextTrigger = baseTime;
        int maxAttempts = 7;
        int attempts = 0;

        while (attempts < maxAttempts) {
            java.time.DayOfWeek dayOfWeek = nextTrigger.getDayOfWeek();
            Reminder.DayOfWeek reminderDay = convertToReminderDay(dayOfWeek);

            if (reminder.getRepeatDays().contains(reminderDay)) {
                return nextTrigger;
            }

            nextTrigger = nextTrigger.plusDays(1);
            attempts++;
        }

        return baseTime;
    }

    /**
     * 判断是否是周末
     */
    private boolean isWeekend(LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY;
    }
}