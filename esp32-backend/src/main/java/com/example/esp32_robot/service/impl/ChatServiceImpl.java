package com.example.esp32_robot.service.impl;

import com.example.esp32_robot.dto.chat.*;
import com.example.esp32_robot.dto.common.PageResponse;
import com.example.esp32_robot.dto.converter.DtoConverter;
import com.example.esp32_robot.entity.Chat;
import com.example.esp32_robot.exception.ResourceNotFoundException;
import com.example.esp32_robot.repository.ChatRepository;
import com.example.esp32_robot.service.ChatService;
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
public class ChatServiceImpl extends BaseService implements ChatService {

    private final ChatRepository chatRepository;
    private final DtoConverter dtoConverter;

    @Override
    public ChatResponse createChat(ChatRequest request) {
        log.info("Creating chat for device: {}, user: {}", request.getDeviceId(), request.getUserId());

        Chat chat = dtoConverter.toChatEntity(request);
        chat.setTimestamp(LocalDateTime.now());

        Chat savedChat = chatRepository.save(chat);
        log.info("Chat created successfully with id: {}", savedChat.getId());

        return dtoConverter.toChatResponse(savedChat);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatResponse getChatById(Long id) {
        log.info("Fetching chat with id: {}", id);

        Chat chat = chatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chat", "id", id));

        return dtoConverter.toChatResponse(chat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByDeviceId(String deviceId) {
        log.info("Fetching chats for device: {}", deviceId);

        List<Chat> chats = chatRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
        return dtoConverter.toChatResponseList(chats);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByUserId(Long userId) {
        log.info("Fetching chats for user: {}", userId);

        List<Chat> chats = chatRepository.findByUserIdOrderByTimestampDesc(userId);
        return dtoConverter.toChatResponseList(chats);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ChatResponse> queryChats(ChatQueryRequest request) {
        log.info("Querying chats with filters");

        Pageable pageable = createPageable(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<Chat> chatPage;

        if (request.getDeviceId() != null && request.getUserId() != null) {
            chatPage = chatRepository.findAll(pageable); // 需要自定义分页查询
        } else if (request.getDeviceId() != null) {
            List<Chat> chats = chatRepository.findByDeviceIdOrderByTimestampDesc(request.getDeviceId());
            chatPage = Page.empty(pageable);
        } else if (request.getUserId() != null) {
            List<Chat> chats = chatRepository.findByUserIdOrderByTimestampDesc(request.getUserId());
            chatPage = Page.empty(pageable);
        } else {
            chatPage = chatRepository.findAll(pageable);
        }

        // 应用额外过滤条件
        List<ChatResponse> filteredResponses = filterAndConvertChats(chatPage.getContent(), request);

        return PageResponse.of(filteredResponses, request.getPage(), request.getSize(),
                (long) filteredResponses.size());
    }

    private List<ChatResponse> filterAndConvertChats(List<Chat> chats, ChatQueryRequest request) {
        return chats.stream()
                .filter(chat -> filterByType(chat, request.getType()))
                .filter(chat -> filterBySentiment(chat, request.getMinSentiment(), request.getMaxSentiment()))
                .filter(chat -> filterByTimeRange(chat, request.getStartTime(), request.getEndTime()))
                .filter(chat -> filterByKeyword(chat, request.getKeyword()))
                .map(dtoConverter::toChatResponse)
                .toList();
    }

    private boolean filterByType(Chat chat, String type) {
        return type == null || type.equals(chat.getType());
    }

    private boolean filterBySentiment(Chat chat, Double minSentiment, Double maxSentiment) {
        if (minSentiment == null && maxSentiment == null) return true;
        if (minSentiment != null && chat.getSentiment() < minSentiment) return false;
        if (maxSentiment != null && chat.getSentiment() > maxSentiment) return false;
        return true;
    }

    private boolean filterByTimeRange(Chat chat, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && chat.getTimestamp().isBefore(startTime)) return false;
        if (endTime != null && chat.getTimestamp().isAfter(endTime)) return false;
        return true;
    }

    private boolean filterByKeyword(Chat chat, String keyword) {
        if (keyword == null) return true;
        return chat.getContent() != null && chat.getContent().toLowerCase().contains(keyword.toLowerCase());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("Fetching chats between {} and {}", startTime, endTime);

        List<Chat> chats = chatRepository.findByTimestampBetweenOrderByTimestampDesc(startTime, endTime);
        return dtoConverter.toChatResponseList(chats);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatStatisticsResponse getChatStatistics(String deviceId) {
        log.info("Calculating chat statistics for device: {}", deviceId);

        List<Chat> chats = chatRepository.findByDeviceIdOrderByTimestampDesc(deviceId);

        long totalMessages = chats.size();
        long textMessages = chats.stream().filter(c -> "text".equals(c.getType())).count();
        long audioMessages = chats.stream().filter(c -> "audio".equals(c.getType())).count();

        double avgSentiment = chats.stream()
                .mapToDouble(Chat::getSentiment)
                .average()
                .orElse(0.5);

        String dominantMood = chats.stream()
                .collect(java.util.stream.Collectors.groupingBy(Chat::getMood,
                        java.util.stream.Collectors.counting()))
                .entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("neutral");

        return new ChatStatisticsResponse(totalMessages, textMessages, audioMessages,
                avgSentiment, dominantMood);
    }

    @Override
    public void deleteChat(Long id) {
        log.info("Deleting chat with id: {}", id);

        if (!chatRepository.existsById(id)) {
            throw new ResourceNotFoundException("Chat", "id", id);
        }

        chatRepository.deleteById(id);
        log.info("Chat deleted successfully");
    }

    @Override
    public void deleteChatsByDeviceId(String deviceId) {
        log.info("Deleting all chats for device: {}", deviceId);

        List<Chat> chats = chatRepository.findByDeviceIdOrderByTimestampDesc(deviceId);
        chatRepository.deleteAll(chats);
        log.info("Deleted {} chats for device: {}", chats.size(), deviceId);
    }
}