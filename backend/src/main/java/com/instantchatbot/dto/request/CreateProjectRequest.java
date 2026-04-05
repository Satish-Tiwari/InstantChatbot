package com.instantchatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

/**
 * Data transfer object for creating a new chatbot project.
 * Captures essential metadata like the project name and the source website URL.
 */
@Data
public class CreateProjectRequest {

    /**
     * User-defined name for the chatbot project.
     */
    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    /**
     * The entry point URL for the web crawler.
     * Must be a valid absolute URL (e.g., https://example.com).
     */
    @NotBlank(message = "Website URL is required")
    @URL(message = "Invalid URL format")
    private String websiteUrl;

    /**
     * User-provided AI provider override, if any.
     */
    private String customAiProvider;

    /**
     * Optional user-provided OpenAI API Key for project isolation.
     */
    private String customOpenAiApiKey;

    /**
     * Optional user-provided Anthropic API Key.
     */
    private String customAnthropicApiKey;

    /**
     * Optional Google Project ID for custom Vertex AI usage.
     */
    private String customGoogleProjectId;

    /**
     * Optional Google Location for custom Vertex AI usage.
     */
    private String customGoogleLocation;
}
