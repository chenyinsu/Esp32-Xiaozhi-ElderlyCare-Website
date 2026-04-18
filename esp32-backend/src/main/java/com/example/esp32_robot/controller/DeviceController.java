package com.example.esp32_robot.controller;

import com.example.esp32_robot.dto.ApiResponse;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.device.*;
import com.example.esp32_robot.entity.Device;
import com.example.esp32_robot.service.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
@Tag(name = "设备管理", description = "ESP32设备相关接口")
public class DeviceController {

    private final DeviceService deviceService;

    @Operation(summary = "注册新设备")
    @PostMapping
    public ApiResponse<DeviceResponse> createDevice(@Valid @RequestBody DeviceRequest request) {
        log.info("REST request to create device: {}", request.getDeviceId());
        DeviceResponse response = deviceService.createDevice(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新设备信息")
    @PutMapping("/{id}")
    public ApiResponse<DeviceResponse> updateDevice(
            @Parameter(description = "设备ID") @PathVariable Long id,
            @Valid @RequestBody DeviceRequest request) {
        log.info("REST request to update device: {}", id);
        DeviceResponse response = deviceService.updateDevice(id, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "根据ID获取设备信息")
    @GetMapping("/{id}")
    public ApiResponse<DeviceResponse> getDeviceById(@Parameter(description = "设备ID") @PathVariable Long id) {
        log.info("REST request to get device by id: {}", id);
        DeviceResponse response = deviceService.getDeviceById(id);
        return ApiResponse.success(response);
    }

    @Operation(summary = "根据设备唯一标识获取设备信息")
    @GetMapping("/device-id/{deviceId}")
    public ApiResponse<DeviceResponse> getDeviceByDeviceId(@Parameter(description = "设备唯一标识") @PathVariable String deviceId) {
        log.info("REST request to get device by deviceId: {}", deviceId);
        DeviceResponse response = deviceService.getDeviceByDeviceId(deviceId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取用户绑定的设备")
    @GetMapping("/user/{userId}")
    public ApiResponse<DeviceResponse> getDeviceByUserId(@Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("REST request to get device by user id: {}", userId);
        DeviceResponse response = deviceService.getDeviceByUserId(userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "分页查询设备")
    @PostMapping("/query")
    public ApiResponse<PageResponse<DeviceResponse>> queryDevices(@RequestBody DeviceQueryRequest request) {
        log.info("REST request to query devices");
        PageResponse<DeviceResponse> response = deviceService.queryDevices(request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取所有在线设备")
    @GetMapping("/online")
    public ApiResponse<List<DeviceResponse>> getOnlineDevices() {
        log.info("REST request to get online devices");
        List<DeviceResponse> responses = deviceService.getOnlineDevices();
        return ApiResponse.success(responses);
    }

    @Operation(summary = "获取低电量设备")
    @GetMapping("/low-battery")
    public ApiResponse<List<DeviceResponse>> getLowBatteryDevices(
            @Parameter(description = "电量阈值") @RequestParam(defaultValue = "20") Integer threshold) {
        log.info("REST request to get low battery devices below {}%", threshold);
        List<DeviceResponse> responses = deviceService.getLowBatteryDevices(threshold);
        return ApiResponse.success(responses);
    }

    @Operation(summary = "更新设备状态（心跳上报）")
    @PatchMapping("/{id}/status")
    public ApiResponse<DeviceResponse> updateDeviceStatus(
            @Parameter(description = "设备ID") @PathVariable Long id,
            @Valid @RequestBody DeviceStatusRequest request) {
        log.info("REST request to update device status: {} to {}", id, request.getStatus());
        DeviceResponse response = deviceService.updateDeviceStatus(id, request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "绑定设备到用户")
    @PostMapping("/{deviceId}/bind/{userId}")
    public ApiResponse<DeviceResponse> bindDeviceToUser(
            @Parameter(description = "设备ID") @PathVariable Long deviceId,
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("REST request to bind device {} to user {}", deviceId, userId);
        DeviceResponse response = deviceService.bindDeviceToUser(deviceId, userId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "解绑设备")
    @PostMapping("/{deviceId}/unbind")
    public ApiResponse<DeviceResponse> unbindDevice(@Parameter(description = "设备ID") @PathVariable Long deviceId) {
        log.info("REST request to unbind device: {}", deviceId);
        DeviceResponse response = deviceService.unbindDevice(deviceId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取设备统计信息")
    @GetMapping("/statistics")
    public ApiResponse<DeviceStatisticsResponse> getDeviceStatistics() {
        log.info("REST request to get device statistics");
        DeviceStatisticsResponse response = deviceService.getDeviceStatistics();
        return ApiResponse.success(response);
    }

    @Operation(summary = "删除设备")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDevice(@Parameter(description = "设备ID") @PathVariable Long id) {
        log.info("REST request to delete device: {}", id);
        deviceService.deleteDevice(id);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "批量更新设备状态")
    @PatchMapping("/batch/status")
    public ApiResponse<Void> batchUpdateDeviceStatus(
            @Parameter(description = "设备ID列表") @RequestBody List<Long> ids,
            @Parameter(description = "状态") @RequestParam Device.Status status) {
        log.info("REST request to batch update device status for {} devices to {}", ids.size(), status);
        deviceService.batchUpdateDeviceStatus(ids, status);
        return ApiResponse.success("批量更新成功", null);
    }
}