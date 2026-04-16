package com.example.esp32_robot.service.impl;

import com.example.esp32_robot.dto.reminder.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.converter.DtoConverter;
import com.example.esp32_robot.entity.*;
import com.example.esp32_robot.exception.BusinessException;
import com.example.esp32_robot.exception.ResourceNotFoundException;
import com.example.esp32_robot.repository.*;
import com.example.esp32_robot.service.ReminderService;
import com.example.esp32_robot.service.base.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    private void calculateNextTriggerTime(Reminder reminder) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextTrigger = LocalDateTime.of(now.toLocalDate(), reminder.getRemindTime());

        if (nextTrigger.isBefore(now)) {
            nextTrigger = nextTrigger.plusDays(1);
        }

        // 根据重复类型调整
        switch (reminder.getRepeatType()) {
            case DAILY:
                // 每天，保持当前计算
                break;
            case WEEKLY:
                // 每周特定几天
                nextTrigger = calculateNextWeeklyTrigger(reminder, nextTrigger);
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
                // 不重复
                break;
        }

        reminder.setNextTrigger(nextTrigger);
    }

    private LocalDateTime calculateNextWeeklyTrigger(Reminder reminder, LocalDateTime baseTime) {
        if (reminder.getRepeatDays() == null || reminder.getRepeatDays().isEmpty()) {
            return baseTime;
        }

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

    private Reminder.DayOfWeek convertToReminderDay(java.time.DayOfWeek dayOfWeek) {
        return Reminder.DayOfWeek.valueOf(dayOfWeek.name());
    }

    private boolean isWeekend(LocalDate date) {
        java.time.DayOfWeek day = date.getDayOfWeek();
        return day == java.time.DayOfWeek.SATURDAY || day == java.time.DayOfWeek.SUNDAY;
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
    @Transactional(readOnly = true)
    public PageResponse<ReminderResponse> queryReminders(ReminderQueryRequest request) {
        log.info("Querying reminders with filters");

        Pageable pageable = createPageable(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.ASC, "remindTime"));

        Page<Reminder> reminderPage;

        if (request.getUserId() != null) {
            List<Reminder> reminders = reminderRepository.findByUserIdOrderByRemindTimeAsc(request.getUserId());
            reminderPage = Page.empty(pageable);
        } else {
            reminderPage = reminderRepository.findAll(pageable);
        }

        List<ReminderResponse> filteredResponses = filterAndConvertReminders(
                reminderPage.getContent(), request);

        return PageResponse.of(filteredResponses, request.getPage(), request.getSize(),
                (long) filteredResponses.size());
    }

    private List<ReminderResponse> filterAndConvertReminders(List<Reminder> reminders,
                                                             ReminderQueryRequest request) {
        return reminders.stream()
                .filter(r -> filterByDeviceId(r, request.getDeviceId()))
                .filter(r -> filterByReminderType(r, request.getReminderType()))
                .filter(r -> filterByRepeatType(r, request.getRepeatType()))
                .filter(r -> filterByIsActive(r, request.getIsActive()))
                .filter(r -> filterByIsTaken(r, request.getIsTaken()))
                .filter(r -> filterByMedicationName(r, request.getMedicationName()))
                .map(dtoConverter::toReminderResponse)
                .toList();
    }

    private boolean filterByDeviceId(Reminder reminder, Long deviceId) {
        return deviceId == null || reminder.getDevice().getId().equals(deviceId);
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
        return isTaken == null ||
                (reminder.getReminderType() != Reminder.ReminderType.MEDICATION) ||
                reminder.getIsTaken().equals(isTaken);
    }

    private boolean filterByMedicationName(Reminder reminder, String medicationName) {
        return medicationName == null ||
                (reminder.getMedicationName() != null &&
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

        List<Reminder> reminders = reminderRepository.findTodayReminders();
        return dtoConverter.toReminderResponseList(reminders);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReminderResponse> getRemindersToTrigger() {
        log.info("Fetching reminders to trigger");

        List<Reminder> reminders = reminderRepository.findRemindersToTrigger(LocalDateTime.now());
        return dtoConverter.toReminderResponseList(reminders);
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
                .toList();

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
    public void deleteReminder(Long id) {
        log.info("Deleting reminder with id: {}", id);

        if (!reminderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reminder", "id", id);
        }

        reminderRepository.deleteById(id);
        log.info("Reminder deleted successfully");
    }

    @Override
    public void batchUpdateReminderStatus(List<Long> ids, Boolean isActive) {
        log.info("Batch updating reminder status for {} reminders to {}", ids.size(), isActive);

        reminderRepository.updateReminderStatus(ids, isActive);
        log.info("Batch update completed");
    }

    /**
     * 定时任务：每小时检查需要触发的提醒
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void triggerReminders() {
        log.info("Running scheduled reminder trigger task");

        List<Reminder> remindersToTrigger = reminderRepository.findRemindersToTrigger(LocalDateTime.now());

        for (Reminder reminder : remindersToTrigger) {
            try {
                triggerReminder(reminder);

                // 更新最后触发时间
                reminder.setLastTriggered(LocalDateTime.now());

                // 计算下次触发时间
                calculateNextTriggerTime(reminder);

                reminderRepository.save(reminder);
            } catch (Exception e) {
                log.error("Failed to trigger reminder: {}", reminder.getId(), e);
            }
        }

        log.info("Triggered {} reminders", remindersToTrigger.size());
    }

    private void triggerReminder(Reminder reminder) {
        log.info("Triggering reminder: {} for user: {}", reminder.getId(), reminder.getUser().getId());

        // 这里可以添加实际的提醒触发逻辑
        // 例如：发送通知、调用设备API播放语音等
    }
}