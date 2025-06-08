package com.example.budget_management_app.chat.config;

import com.example.budget_management_app.chat.constants.Constants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public ChatClient openAIChatClient(ChatClient.Builder builder,
                                       JdbcChatMemoryRepository jdbcChatMemoryRepository){

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(Constants.MAX_MESSAGE_WINDOW)
                .build();

        return builder
                .defaultSystem(Constants.HOUSEHOLD_BUDGET_SYSTEM_MESSAGE)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
