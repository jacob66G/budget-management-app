package com.example.budget_management_app.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService{

    private final ChatClient chatClient;

    /**
     * @param message
     * @return
     */
    @Override
    public String generateResponse(String message) {
        return this.chatClient
                .prompt(message)
                .call()
                .content();
    }
}
