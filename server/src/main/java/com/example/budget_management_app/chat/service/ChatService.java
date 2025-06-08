package com.example.budget_management_app.chat.service;

import com.example.budget_management_app.chat.dto.Chat;
import com.example.budget_management_app.chat.dto.ChatMessage;
import com.example.budget_management_app.chat.dto.ChatStartResponse;

import java.util.List;

public interface ChatService {

    ChatStartResponse createChat(Long userId, String message);

    List<Chat> getAllChatsForUser(Long userId);

    List<ChatMessage> getChatMessages(String id, Long userId);

    String chat(String chatId, Long userId, String message, boolean justCreated);
}
