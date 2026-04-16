package com.example.esp32_robot.service;

import com.example.esp32_robot.dto.device.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.entity.Device;

import java.util.List;

public interface DeviceService {

    DeviceResponse createDevice(DeviceRequest request);

    DeviceResponse updateDevice(Long id, DeviceRequest request);

    DeviceResponse getDeviceById(Long id);

    DeviceResponse getDeviceByDeviceId(String deviceId);

    DeviceResponse getDeviceByUserId(Long userId);

    PageResponse<DeviceResponse> queryDevices(DeviceQueryRequest request);

    List<DeviceResponse> getOnlineDevices();

    List<DeviceResponse> getLowBatteryDevices(Integer threshold);

    DeviceResponse updateDeviceStatus(Long id, DeviceStatusRequest request);

    DeviceResponse bindDeviceToUser(Long deviceId, Long userId);

    DeviceResponse unbindDevice(Long deviceId);

    DeviceStatisticsResponse getDeviceStatistics();

    void deleteDevice(Long id);

    void batchUpdateDeviceStatus(List<Long> ids, Device.Status status);
}