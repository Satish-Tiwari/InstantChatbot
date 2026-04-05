package com.instantchatbot.dto.response;

import com.instantchatbot.entity.CrawlStatus;
import com.instantchatbot.entity.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message DTO sent over WebSockets during crawling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlProgressUpdate {
    private Long projectId;
    private ProjectStatus projectStatus;
    private CrawlStatus crawlStatus;
    private int pagesFound;
    private int pagesProcessed;
    private int chunksCreated;
    private String currentUrl;
    private String errorMessage;
}
