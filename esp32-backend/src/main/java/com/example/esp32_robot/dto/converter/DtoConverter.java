package com.example.esp32_robot.dto.converter;

import com.example.esp32_robot.dto.chat.*;
import com.example.esp32_robot.dto.device.*;
import com.example.esp32_robot.dto.emergency.*;
import com.example.esp32_robot.dto.emergencyrecord.*;
import com.example.esp32_robot.dto.notification.*;
import com.example.esp32_robot.dto.reminder.*;
import com.example.esp32_robot.dto.report.*;
import com.example.esp32_robot.dto.user.*;
import com.example.esp32_robot.entity.*;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoConverter {

    // Chat conversions
    public ChatResponse toChatResponse(Chat chat) {
        ChatResponse response = new ChatResponse();
        BeanUtils.copyProperties(chat, response);
        return response;
    }

    public Chat toChatEntity(ChatRequest request) {
        Chat chat = new Chat();
        BeanUtils.copyProperties(request, chat);
        return chat;
    }

    public List<ChatResponse> toChatResponseList(List<Chat> chats) {
        return chats.stream().map(this::toChatResponse).collect(Collectors.toList());
    }

    // Device conversions
    public DeviceResponse toDeviceResponse(Device device) {
        DeviceResponse response = new DeviceResponse();
        BeanUtils.copyProperties(device, response);
        if (device.getUser() != null) {
            response.setUserId(device.getUser().getId());
            response.setUserName(device.getUser().getName());
        }
        response.setReminderCount((long) device.getReminders().size());
        response.setEmergencyCount((long) device.getEmergencies().size());
        return response;
    }

    public Device toDeviceEntity(DeviceRequest request) {
        Device device = new Device();
        BeanUtils.copyProperties(request, device);
        return device;
    }

    public void updateDeviceEntity(DeviceRequest request, Device device) {
        BeanUtils.copyProperties(request, device, "id", "createdAt", "updatedAt");
    }

    public List<DeviceResponse> toDeviceResponseList(List<Device> devices) {
        return devices.stream().map(this::toDeviceResponse).collect(Collectors.toList());
    }

    // Emergency conversions
    public EmergencyResponse toEmergencyResponse(Emergency emergency) {
        EmergencyResponse response = new EmergencyResponse();
        BeanUtils.copyProperties(emergency, response);

        if (emergency.getDevice() != null) {
            response.setDeviceId(emergency.getDevice().getId());
            response.setDeviceName(emergency.getDevice().getDeviceName());
        }
        if (emergency.getUser() != null) {
            response.setUserId(emergency.getUser().getId());
            response.setUserName(emergency.getUser().getName());
        }
        if (emergency.getHandledBy() != null) {
            response.setHandledById(emergency.getHandledBy().getId());
            response.setHandledByName(emergency.getHandledBy().getName());
        }
        response.setRecordCount((long) emergency.getRecords().size());

        return response;
    }

    public Emergency toEmergencyEntity(EmergencyRequest request) {
        Emergency emergency = new Emergency();
        BeanUtils.copyProperties(request, emergency);
        return emergency;
    }

    public List<EmergencyResponse> toEmergencyResponseList(List<Emergency> emergencies) {
        return emergencies.stream().map(this::toEmergencyResponse).collect(Collectors.toList());
    }

    // EmergencyRecord conversions
    public EmergencyRecordResponse toEmergencyRecordResponse(EmergencyRecord record) {
        EmergencyRecordResponse response = new EmergencyRecordResponse();
        BeanUtils.copyProperties(record, response);

        if (record.getEmergency() != null) {
            response.setEmergencyId(record.getEmergency().getId());
        }
        if (record.getUser() != null) {
            response.setUserId(record.getUser().getId());
            response.setUserName(record.getUser().getName());
        }

        return response;
    }

    public EmergencyRecord toEmergencyRecordEntity(EmergencyRecordRequest request) {
        EmergencyRecord record = new EmergencyRecord();
        BeanUtils.copyProperties(request, record);
        return record;
    }

    public List<EmergencyRecordResponse> toEmergencyRecordResponseList(List<EmergencyRecord> records) {
        return records.stream().map(this::toEmergencyRecordResponse).collect(Collectors.toList());
    }

    // Notification conversions
    public NotificationResponse toNotificationResponse(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        BeanUtils.copyProperties(notification, response);

        if (notification.getSender() != null) {
            response.setSenderId(notification.getSender().getId());
            response.setSenderName(notification.getSender().getName());
        }
        if (notification.getReceiver() != null) {
            response.setReceiverId(notification.getReceiver().getId());
            response.setReceiverName(notification.getReceiver().getName());
        }
        response.setIsExpired(notification.isExpired());

        return response;
    }

    public Notification toNotificationEntity(NotificationRequest request) {
        Notification notification = new Notification();
        BeanUtils.copyProperties(request, notification);
        return notification;
    }

    public List<NotificationResponse> toNotificationResponseList(List<Notification> notifications) {
        return notifications.stream().map(this::toNotificationResponse).collect(Collectors.toList());
    }

    // Reminder conversions
    public ReminderResponse toReminderResponse(Reminder reminder) {
        ReminderResponse response = new ReminderResponse();
        BeanUtils.copyProperties(reminder, response);

        if (reminder.getDevice() != null) {
            response.setDeviceId(reminder.getDevice().getId());
            response.setDeviceName(reminder.getDevice().getDeviceName());
        }
        if (reminder.getUser() != null) {
            response.setUserId(reminder.getUser().getId());
            response.setUserName(reminder.getUser().getName());
        }

        return response;
    }

    public Reminder toReminderEntity(ReminderRequest request) {
        Reminder reminder = new Reminder();
        BeanUtils.copyProperties(request, reminder);
        return reminder;
    }

    public void updateReminderEntity(ReminderRequest request, Reminder reminder) {
        BeanUtils.copyProperties(request, reminder, "id", "createdAt", "updatedAt", "lastTriggered", "nextTrigger");
    }

    public List<ReminderResponse> toReminderResponseList(List<Reminder> reminders) {
        return reminders.stream().map(this::toReminderResponse).collect(Collectors.toList());
    }

    // Report conversions
    public ReportResponse toReportResponse(Report report) {
        ReportResponse response = new ReportResponse();
        BeanUtils.copyProperties(report, response);

        if (report.getDevice() != null) {
            response.setDeviceId(report.getDevice().getId());
            response.setDeviceName(report.getDevice().getDeviceName());
        }
        if (report.getUser() != null) {
            response.setUserId(report.getUser().getId());
            response.setUserName(report.getUser().getName());
        }

        return response;
    }

    public Report toReportEntity(ReportRequest request) {
        Report report = new Report();
        BeanUtils.copyProperties(request, report);
        return report;
    }

    public void updateReportEntity(ReportRequest request, Report report) {
        BeanUtils.copyProperties(request, report, "id", "createdAt", "updatedAt");
    }

    public List<ReportResponse> toReportResponseList(List<Report> reports) {
        return reports.stream().map(this::toReportResponse).collect(Collectors.toList());
    }

    // User conversions
    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        BeanUtils.copyProperties(user, response, "password");
        return response;
    }

    public UserDetailResponse toUserDetailResponse(User user) {
        UserDetailResponse response = new UserDetailResponse();
        BeanUtils.copyProperties(user, response, "password", "managedElders", "emergencyContacts");

        if (user.getManagedElders() != null) {
            response.setManagedElders(user.getManagedElders().stream()
                    .map(this::toUserSummaryResponse)
                    .collect(Collectors.toList()));
        }
        if (user.getEmergencyContacts() != null) {
            response.setEmergencyContacts(user.getEmergencyContacts().stream()
                    .map(this::toUserSummaryResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    public UserSummaryResponse toUserSummaryResponse(User user) {
        UserSummaryResponse response = new UserSummaryResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    public User toUserEntity(UserRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user, "managedElderIds", "emergencyContactIds");
        return user;
    }

    public void updateUserEntity(UserRequest request, User user) {
        BeanUtils.copyProperties(request, user, "id", "password", "createdAt", "updatedAt",
                "managedElders", "emergencyContacts");
    }

    public LoginResponse toLoginResponse(User user, String token) {
        return LoginResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .phone(user.getPhone())
                .role(user.getRole())
                .token(token)
                .deviceId(user.getDeviceId())
                .build();
    }

    public List<UserResponse> toUserResponseList(List<User> users) {
        return users.stream().map(this::toUserResponse).collect(Collectors.toList());
    }

    public List<UserSummaryResponse> toUserSummaryResponseList(List<User> users) {
        return users.stream().map(this::toUserSummaryResponse).collect(Collectors.toList());
    }
}