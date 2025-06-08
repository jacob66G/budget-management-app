package com.example.budget_management_app.chat.dao;

import com.example.budget_management_app.chat.dto.Chat;
import com.example.budget_management_app.chat.dto.ChatMessage;

import java.util.List;

public interface ChatMemoryDao {

    String createChat(Long userId, String description);

    boolean chatExists(String id, Long userId);

    List<Chat> getAllChatsForUser(Long userId);

    List<ChatMessage> getChatMessages(String id);

    int updateLastUsage(String id);
}
