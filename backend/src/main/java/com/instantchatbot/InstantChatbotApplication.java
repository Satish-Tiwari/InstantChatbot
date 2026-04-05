package com.instantchatbot;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main entry point for the Instant Chatbot AI Backend.
 * Initializes the Spring Boot context, enables asynchronous processing,
 * and boots the RAG pipeline components.
 */
@SpringBootApplication
@EnableAsync
public class InstantChatbotApplication {

    /**
     * Bootstraps the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Load .env variables into System Properties for Spring Boot to pick up
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> {
            if (System.getProperty(entry.getKey()) == null) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });

        SpringApplication.run(InstantChatbotApplication.class, args);
    }
}
