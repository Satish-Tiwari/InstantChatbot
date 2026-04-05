package com.instantchatbot.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

@Configuration
public class AiProviderConfig {

    @Value("${spring.ai.provider:openai}")
    private String provider;

    @Bean
    @Primary
    public ChatClient.Builder primaryChatClientBuilder(List<ChatModel> chatModels) {
        // Find the chat model that matches the provider
        ChatModel selectedModel = chatModels.stream()
                .filter(model -> {
                    String className = model.getClass().getSimpleName().toLowerCase();
                    return switch (provider.toLowerCase()) {
                        case "openai" -> className.contains("openai");
                        case "anthropic" -> className.contains("anthropic");
                        case "vertex", "google" -> className.contains("vertexai") || className.contains("gemini");
                        default -> false;
                    };
                })
                .findFirst()
                .orElse(chatModels.isEmpty() ? null : chatModels.get(0));

        if (selectedModel == null) {
            throw new IllegalStateException("No ChatModel found for provider: " + provider);
        }

        return ChatClient.builder(selectedModel);
    }
}
