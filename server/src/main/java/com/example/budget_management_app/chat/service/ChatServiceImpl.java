package com.example.budget_management_app.chat.service;

import com.example.budget_management_app.chat.dao.ChatMemoryDao;
import com.example.budget_management_app.chat.dto.Chat;
import com.example.budget_management_app.chat.dto.ChatMessage;
import com.example.budget_management_app.chat.dto.ChatStartResponse;
import com.example.budget_management_app.common.exception.ErrorCode;
import com.example.budget_management_app.common.exception.InternalException;
import com.example.budget_management_app.common.exception.NotFoundException;
import com.example.budget_management_app.user.dao.UserDao;
import com.example.budget_management_app.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    @Value("classpath:ai-prompts/system-message-description.st")
    private Resource descriptionCreatorSystemMessage;

    private final ChatClient chatClient;
    private final ChatMemoryDao chatMemoryDao;
    private final UserDao userDao;
    /**
     * @param userId
     * @param message
     * @return ChatStartResponse object with generated chatId, new assistance message and short chat description
     */
    @Override
    public ChatStartResponse createChat(Long userId, String message) {
        if (!userDao.userExists(userId)) {
            throw new NotFoundException(User.class.getSimpleName(), userId, ErrorCode.USER_NOT_FOUND);
        }
        String description = this.generateDescription(message);
        String chatId = this.chatMemoryDao.createChat(userId, description);
        String response = this.chat(chatId, userId, message, true);
        return new ChatStartResponse(chatId, response, description);
    }

    /**
     * @return
     */
    @Override
    public List<Chat> getAllChatsForUser(Long userId) {
        return this.chatMemoryDao.getAllChatsForUser(userId);
    }

    /**
     * @param id
     * @return List of ChatMessage objects
     */
    @Override
    public List<ChatMessage> getChatMessages(String id, Long userId) {

        if (!this.chatMemoryDao.chatExists(id, userId)) {
            throw new NotFoundException("Chat", id, ErrorCode.CHAT_NOT_FOUND);
        }

        return this.chatMemoryDao.getChatMessages(id);
    }

    /**
     * @param chatId
     * @param message
     * @return Assistance message
     */
    @Override
    public String chat(String chatId, Long userId, String message, boolean justCreated) {

        String today = LocalDate.now().toString();

        if (!this.chatMemoryDao.chatExists(chatId, userId)) {
            throw new NotFoundException("Chat", chatId, ErrorCode.CHAT_NOT_FOUND);
        }

        if (!justCreated) {
            int affectedRows = this.chatMemoryDao.updateLastUsage(chatId);
            if (affectedRows == 0) {
                throw new InternalException("An error occurred during updating chat with id: " + chatId);
            }
        }

        return this.chatClient
                .prompt(message)
                .system( s -> s.param("current_date", today))
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();
    }

    /**
     * @param message
     * @return new generated description
     */

    private String generateDescription(String message) {
        return this.chatClient
                .prompt()
                .system(descriptionCreatorSystemMessage)
                .user(message)
                .call()
                .content();
    }
}
