package com.instantchatbot.dto.response;

import com.instantchatbot.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Comprehensive project details for UI display and status monitoring.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String websiteUrl;
    /** Overall project lifecycle status */
    private ProjectStatus status;
    private Integer pagesFound;
    private Integer chunksCreated;
    /** Nested details of the most recent crawl job, if any */
    private CrawlJobResponse crawlJob;
    /** Computed flag indicating if the downloadable bot package is available */
    private boolean downloadReady;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** User-provided AI provider override, if any */
    private String customAiProvider;
    
    /** Flag indicating if a custom OpenAI key is currently configured */
    private boolean hasCustomOpenAiKey;
    
    /** Flag indicating if a custom Anthropic key is currently configured */
    private boolean hasCustomAnthropicKey;
    
    /** Flag indicating if a custom Google/Vertex AI configuration is present */
    private boolean hasCustomGoogleKey;
}
