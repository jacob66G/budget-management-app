package com.example.budget_management_app.chat.dao;

import com.example.budget_management_app.chat.dto.Chat;
import com.example.budget_management_app.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMemoryDaoImpl implements ChatMemoryDao{

    private final JdbcTemplate jdbcTemplate;
    /**
     * @param userId The user who owns this chat
     * @param description A short name for the chat
     * @return The generated conversation_id (UUID) for this chat
     */
    @Override
    public String createChat(Long userId, String description) {
        String query = "INSERT INTO chat_memory (user_id, description) VALUES (?, ?) RETURNING conversation_id";
        return jdbcTemplate.queryForObject(query, String.class, userId, description);
    }

    /**
     * @param id The conversation_id to check
     * @return true if the chat exists, false otherwise
     */
    @Override
    public boolean chatExists(String id, Long userId) {
        String query = "SELECT COUNT(*) FROM chat_memory WHERE conversation_id = ?::uuid AND user_id = ?::bigint ";
        Integer count = jdbcTemplate.queryForObject(query, Integer.class, id, userId);
        return count != null && count == 1;
    }

    /**
     * @param userId The user whose chats to retrieve
     * @return List of Chat objects with id and description
     */
    @Override
    public List<Chat> getAllChatsForUser(Long userId) {
        String query = "SELECT conversation_id, description FROM chat_memory WHERE user_id = ?::bigint ORDER BY last_usage DESC";
        return jdbcTemplate.query(query, (rs, rowNum) ->
                new Chat(rs.getString("conversation_id"), rs.getString("description")), userId);
    }

    /**
     * @param id The conversation_id to get messages for
     * @return List of ChatMessage objects in chronological order
     */
    @Override
    public List<ChatMessage> getChatMessages(String id) {
        String query = "SELECT content, type FROM spring_ai_chat_memory WHERE conversation_id = ? ORDER BY timestamp DESC";
        return jdbcTemplate.query(query, (rs, rowNum) ->
                new ChatMessage(rs.getString("content"), rs.getString("type")), id);
    }

    /**
     * @param id
     */
    @Override
    public int updateLastUsage(String id) {

        String query = "UPDATE chat_memory SET last_usage = ? WHERE conversation_id = ?::uuid";
        LocalDateTime now = LocalDateTime.now();
        return jdbcTemplate.update(query, now, id);
    }
}
