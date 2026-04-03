package com.instantchatbot.exception;

/**
 * Exception thrown when a requested resource (User, Project, etc.) cannot be found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
