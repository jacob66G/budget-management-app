package com.example.budget_management_app.chat.config;

import com.example.budget_management_app.chat.constants.Constants;
import com.example.budget_management_app.chat.service.SpendingAnalysisService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class Config {

    @Value("classpath:ai-prompts/system-message-assistant.st")
    private Resource financialAssistantSystemMessage;

    @Bean
    public ChatClient openAIChatClient(ChatClient.Builder builder,
                                       JdbcChatMemoryRepository jdbcChatMemoryRepository,
                                       SpendingAnalysisService analysisServicel){

        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(Constants.MAX_MESSAGE_WINDOW)
                .build();

        return builder
                .defaultSystem(financialAssistantSystemMessage)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(analysisServicel)
                .build();
    }
}
