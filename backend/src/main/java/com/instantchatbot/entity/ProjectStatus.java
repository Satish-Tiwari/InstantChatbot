package com.instantchatbot.entity;

/**
 * Defines the overall lifecycle stages of a chatbot project.
 */
public enum ProjectStatus {
    PENDING,
    CRAWLING,
    PROCESSING,
    EMBEDDING,
    GENERATING,
    READY,
    FAILED
}
