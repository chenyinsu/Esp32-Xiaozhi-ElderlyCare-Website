package com.example.esp32_robot.controller;

import com.example.esp32_robot.dto.ApiResponse;
import com.example.esp32_robot.dto.ChatDTO;
import com.example.esp32_robot.entity.Chat;
import com.example.esp32_robot.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ApiResponse<Chat> createChat(@Valid @RequestBody ChatDTO dto) {
        Chat chat = chatService.processChat(dto);
        return ApiResponse.success(chat);
    }

    @GetMapping
    public ApiResponse<List<Chat>> getChats(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) Long userId) {

        List<Chat> chats = chatService.getChats(deviceId, userId);
        return ApiResponse.success(chats);
    }

    @GetMapping("/{id}")
    public ApiResponse<Chat> getChat(@PathVariable Long id) {
        Chat chat = chatService.getChat(id);
        return ApiResponse.success(chat);
    }
}