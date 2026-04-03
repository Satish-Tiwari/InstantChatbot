package com.instantchatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Data transfer object for an interactive chat request.
 * Contains the user's natural language message targeted at the project's knowledge base.
 */
@Data
public class ChatRequest {

    /**
     * The raw text message sent by the user.
     * Must be non-blank and within reasonable character limits for processing.
     */
    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;
}
