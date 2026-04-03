package com.instantchatbot.exception;

/**
 * Exception thrown when a client request contains invalid data or violates business rules.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
