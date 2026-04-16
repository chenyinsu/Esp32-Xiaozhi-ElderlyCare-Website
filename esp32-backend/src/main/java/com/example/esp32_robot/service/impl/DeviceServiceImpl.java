package com.example.esp32_robot.service.impl;

import com.example.esp32_robot.dto.device.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.converter.DtoConverter;
import com.example.esp32_robot.entity.Device;
import com.example.esp32_robot.entity.User;
import com.example.esp32_robot.exception.BusinessException;
import com.example.esp32_robot.exception.ResourceNotFoundException;
import com.example.esp32_robot.repository.DeviceRepository;
import com.example.esp32_robot.repository.UserRepository;
import com.example.esp32_robot.service.DeviceService;
import com.example.esp32_robot.service.base.BaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DeviceServiceImpl extends BaseService implements DeviceService {

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final DtoConverter dtoConverter;

    @Override
    public DeviceResponse createDevice(DeviceRequest request) {
        log.info("Creating device with id: {}", request.getDeviceId());

        // 检查设备ID是否已存在
        deviceRepository.findByDeviceId(request.getDeviceId()).ifPresent(d -> {
            throw new BusinessException("Device already exists with id: " + request.getDeviceId());
        });

        Device device = dtoConverter.toDeviceEntity(request);

        // 如果指定了用户，绑定用户
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

            // 检查用户是否已经绑定了其他设备
            if (user.getDeviceId() != null && !user.getDeviceId().equals(device.getId())) {
                throw new BusinessException("User already has a device bound");
            }

            device.setUser(user);
            user.setDeviceId(device.getId());
        }

        Device savedDevice = deviceRepository.save(device);
        log.info("Device created successfully with id: {}", savedDevice.getId());

        return dtoConverter.toDeviceResponse(savedDevice);
    }

    @Override
    public DeviceResponse updateDevice(Long id, DeviceRequest request) {
        log.info("Updating device with id: {}", id);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        // 更新设备信息
        dtoConverter.updateDeviceEntity(request, device);

        // 处理用户绑定变更
        handleUserBinding(device, request.getUserId());

        Device updatedDevice = deviceRepository.save(device);
        log.info("Device updated successfully");

        return dtoConverter.toDeviceResponse(updatedDevice);
    }

    private void handleUserBinding(Device device, Long newUserId) {
        Long currentUserId = device.getUser() != null ? device.getUser().getId() : null;

        if (newUserId != null && !newUserId.equals(currentUserId)) {
            // 解除旧用户的绑定
            if (currentUserId != null) {
                User oldUser = device.getUser();
                oldUser.setDeviceId(null);
                userRepository.save(oldUser);
            }

            // 绑定新用户
            User newUser = userRepository.findById(newUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", newUserId));

            if (newUser.getDeviceId() != null && !newUser.getDeviceId().equals(device.getId())) {
                throw new BusinessException("User already has a device bound");
            }

            device.setUser(newUser);
            newUser.setDeviceId(device.getId());
            userRepository.save(newUser);
        } else if (newUserId == null && currentUserId != null) {
            // 解除绑定
            User oldUser = device.getUser();
            oldUser.setDeviceId(null);
            userRepository.save(oldUser);
            device.setUser(null);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceById(Long id) {
        log.info("Fetching device with id: {}", id);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        return dtoConverter.toDeviceResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceByDeviceId(String deviceId) {
        log.info("Fetching device with deviceId: {}", deviceId);

        Device device = deviceRepository.findByDeviceId(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "deviceId", deviceId));

        return dtoConverter.toDeviceResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceResponse getDeviceByUserId(Long userId) {
        log.info("Fetching device for user: {}", userId);

        Device device = deviceRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "userId", userId));

        return dtoConverter.toDeviceResponse(device);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DeviceResponse> queryDevices(DeviceQueryRequest request) {
        log.info("Querying devices with filters");

        Pageable pageable = createPageable(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "updatedAt"));

        Page<Device> devicePage;

        if (request.getDeviceId() != null) {
            devicePage = deviceRepository.findByDeviceId(request.getDeviceId())
                    .map(Page::of).orElse(Page.empty(pageable));
        } else if (request.getStatus() != null) {
            List<Device> devices = deviceRepository.findByStatus(request.getStatus());
            devicePage = Page.empty(pageable);
        } else {
            devicePage = deviceRepository.findAll(pageable);
        }

        List<DeviceResponse> filteredResponses = filterAndConvertDevices(devicePage.getContent(), request);

        return PageResponse.of(filteredResponses, request.getPage(), request.getSize(),
                (long) filteredResponses.size());
    }

    private List<DeviceResponse> filterAndConvertDevices(List<Device> devices, DeviceQueryRequest request) {
        return devices.stream()
                .filter(device -> filterByDeviceName(device, request.getDeviceName()))
                .filter(device -> filterByHasCamera(device, request.getHasCamera()))
                .filter(device -> filterByBoundStatus(device, request.getBoundToUser()))
                .filter(device -> filterByBattery(device, request.getMinBattery(), request.getMaxBattery()))
                .filter(device -> filterByLocation(device, request))
                .map(dtoConverter::toDeviceResponse)
                .toList();
    }

    private boolean filterByDeviceName(Device device, String deviceName) {
        return deviceName == null ||
                device.getDeviceName().toLowerCase().contains(deviceName.toLowerCase());
    }

    private boolean filterByHasCamera(Device device, Boolean hasCamera) {
        return hasCamera == null || device.getHasCamera().equals(hasCamera);
    }

    private boolean filterByBoundStatus(Device device, Boolean boundToUser) {
        if (boundToUser == null) return true;
        boolean isBound = device.getUser() != null;
        return boundToUser.equals(isBound);
    }

    private boolean filterByBattery(Device device, Integer minBattery, Integer maxBattery) {
        if (device.getBatteryLevel() == null) return true;
        if (minBattery != null && device.getBatteryLevel() < minBattery) return false;
        if (maxBattery != null && device.getBatteryLevel() > maxBattery) return false;
        return true;
    }

    private boolean filterByLocation(Device device, DeviceQueryRequest request) {
        if (device.getLatitude() == null || device.getLongitude() == null) return true;
        if (request.getMinLatitude() != null && device.getLatitude() < request.getMinLatitude()) return false;
        if (request.getMaxLatitude() != null && device.getLatitude() > request.getMaxLatitude()) return false;
        if (request.getMinLongitude() != null && device.getLongitude() < request.getMinLongitude()) return false;
        if (request.getMaxLongitude() != null && device.getLongitude() > request.getMaxLongitude()) return false;
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getOnlineDevices() {
        log.info("Fetching online devices");

        List<Device> devices = deviceRepository.findByStatus(Device.Status.ONLINE);
        return dtoConverter.toDeviceResponseList(devices);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceResponse> getLowBatteryDevices(Integer threshold) {
        log.info("Fetching low battery devices below {}%", threshold);

        List<Device> devices = deviceRepository.findByBatteryLevelBetween(0, threshold);
        return dtoConverter.toDeviceResponseList(devices);
    }

    @Override
    public DeviceResponse updateDeviceStatus(Long id, DeviceStatusRequest request) {
        log.info("Updating device status for id: {} to {}", id, request.getStatus());

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        device.setStatus(request.getStatus());

        if (request.getStatus() == Device.Status.ONLINE) {
            device.setLastOnlineTime(LocalDateTime.now());
        }

        if (request.getBatteryLevel() != null) {
            device.setBatteryLevel(request.getBatteryLevel());
        }

        if (request.getLatitude() != null && request.getLongitude() != null) {
            device.setLatitude(request.getLatitude());
            device.setLongitude(request.getLongitude());
            device.setLastLocationTime(LocalDateTime.now());
        }

        if (request.getWifiStrength() != null) {
            device.setWifiStrength(request.getWifiStrength());
        }

        Device updatedDevice = deviceRepository.save(device);
        return dtoConverter.toDeviceResponse(updatedDevice);
    }

    @Override
    public DeviceResponse bindDeviceToUser(Long deviceId, Long userId) {
        log.info("Binding device {} to user {}", deviceId, userId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (device.getUser() != null) {
            throw new BusinessException("Device is already bound to a user");
        }

        if (user.getDeviceId() != null) {
            throw new BusinessException("User already has a device bound");
        }

        device.setUser(user);
        user.setDeviceId(device.getId());

        deviceRepository.save(device);
        userRepository.save(user);

        return dtoConverter.toDeviceResponse(device);
    }

    @Override
    public DeviceResponse unbindDevice(Long deviceId) {
        log.info("Unbinding device {}", deviceId);

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", deviceId));

        if (device.getUser() != null) {
            User user = device.getUser();
            user.setDeviceId(null);
            userRepository.save(user);
            device.setUser(null);
        }

        Device savedDevice = deviceRepository.save(device);
        return dtoConverter.toDeviceResponse(savedDevice);
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceStatisticsResponse getDeviceStatistics() {
        log.info("Calculating device statistics");

        List<Device> allDevices = deviceRepository.findAll();

        long totalDevices = allDevices.size();
        long onlineDevices = allDevices.stream().filter(d -> d.getStatus() == Device.Status.ONLINE).count();
        long offlineDevices = allDevices.stream().filter(d -> d.getStatus() == Device.Status.OFFLINE).count();
        long maintenanceDevices = allDevices.stream().filter(d -> d.getStatus() == Device.Status.MAINTENANCE).count();
        long errorDevices = allDevices.stream().filter(d -> d.getStatus() == Device.Status.ERROR).count();
        long lowBatteryDevices = allDevices.stream()
                .filter(d -> d.getBatteryLevel() != null && d.getBatteryLevel() < 20).count();
        long unboundDevices = allDevices.stream().filter(d -> d.getUser() == null).count();
        long cameraDevices = allDevices.stream().filter(Device::getHasCamera).count();

        return DeviceStatisticsResponse.builder()
                .totalDevices(totalDevices)
                .onlineDevices(onlineDevices)
                .offlineDevices(offlineDevices)
                .maintenanceDevices(maintenanceDevices)
                .errorDevices(errorDevices)
                .lowBatteryDevices(lowBatteryDevices)
                .unboundDevices(unboundDevices)
                .cameraDevices(cameraDevices)
                .build();
    }

    @Override
    public void deleteDevice(Long id) {
        log.info("Deleting device with id: {}", id);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device", "id", id));

        // 解除用户绑定
        if (device.getUser() != null) {
            User user = device.getUser();
            user.setDeviceId(null);
            userRepository.save(user);
        }

        deviceRepository.delete(device);
        log.info("Device deleted successfully");
    }

    @Override
    public void batchUpdateDeviceStatus(List<Long> ids, Device.Status status) {
        log.info("Batch updating device status for {} devices to {}", ids.size(), status);

        deviceRepository.updateDeviceStatus(ids, status);
        log.info("Batch update completed");
    }
}