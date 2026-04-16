package com.example.esp32_robot.service;

import com.example.esp32_robot.dto.chat.*;
import com.example.esp32_robot.dto.common.PageResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatService {

    ChatResponse createChat(ChatRequest request);

    ChatResponse getChatById(Long id);

    List<ChatResponse> getChatsByDeviceId(String deviceId);

    List<ChatResponse> getChatsByUserId(Long userId);

    PageResponse<ChatResponse> queryChats(ChatQueryRequest request);

    List<ChatResponse> getChatsByTimeRange(LocalDateTime startTime, LocalDateTime endTime);

    ChatStatisticsResponse getChatStatistics(String deviceId);

    void deleteChat(Long id);

    void deleteChatsByDeviceId(String deviceId);
}
