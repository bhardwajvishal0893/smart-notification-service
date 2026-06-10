package com.vishal.notification.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(OpenAiChatModel model) {
        return ChatClient.builder(model)
                .defaultSystem("""
                        You are a notification routing engine for a smart notification service.
                        Your job is to decide the most appropriate delivery channel for notifications.
                        Always respond with valid JSON only. No markdown, no explanation outside JSON.
                        """)
                .build();
    }
}
