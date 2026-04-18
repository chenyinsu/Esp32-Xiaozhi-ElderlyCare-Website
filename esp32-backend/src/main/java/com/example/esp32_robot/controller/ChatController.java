package com.example.esp32_robot.controller;

import com.example.esp32_robot.dto.ApiResponse;
import com.example.esp32_robot.dto.chat.ChatRequest;
import com.example.esp32_robot.dto.chat.ChatResponse;
import com.example.esp32_robot.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ApiResponse<ChatResponse> createChat(@Valid @RequestBody ChatRequest request) {
        log.info("REST request to create chat");
        ChatResponse response = chatService.createChat(request);
        return ApiResponse.success(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<ChatResponse> getChatById(@PathVariable Long id) {
        log.info("REST request to get chat by id: {}", id);
        ChatResponse response = chatService.getChatById(id);
        return ApiResponse.success(response);
    }

    @GetMapping("/device/{deviceId}")
    public ApiResponse<List<ChatResponse>> getChatsByDeviceId(@PathVariable String deviceId) {
        log.info("REST request to get chats by device id: {}", deviceId);
        List<ChatResponse> responses = chatService.getChatsByDeviceId(deviceId);
        return ApiResponse.success(responses);
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<ChatResponse>> getChatsByUserId(@PathVariable Long userId) {
        log.info("REST request to get chats by user id: {}", userId);
        List<ChatResponse> responses = chatService.getChatsByUserId(userId);
        return ApiResponse.success(responses);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteChat(@PathVariable Long id) {
        log.info("REST request to delete chat: {}", id);
        chatService.deleteChat(id);
        return ApiResponse.success(null);
    }
}