package com.instantchatbot;

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
        SpringApplication.run(InstantChatbotApplication.class, args);
    }
}
