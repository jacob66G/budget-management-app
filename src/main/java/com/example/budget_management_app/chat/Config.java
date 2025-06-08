package com.example.budget_management_app.chat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

    @Bean
    public ChatClient openAIChatClient(ChatClient.Builder builder){
        return builder.build();
    }
}
