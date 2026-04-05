package com.instantchatbot.dto.response;

import com.instantchatbot.entity.CrawlStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Detailed status information for a recurring or active crawl job.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrawlJobResponse {
    private Long id;
    /** Total unique internal pages discovered so far */
    private Integer pagesFound;
    /** Total pages processed through cleaning and chunking */
    private Integer pagesProcessed;
    /** Total vector chunks successfully embedded */
    private Integer chunksCreated;
    /** Current operational status of the job */
    private CrawlStatus status;
    /** Error description if the job failed */
    private String errorMessage;
    /** The URL currently being crawled for real-time progress display */
    private String currentUrl;
    /** Timestamp when the job was queued/started */
    private LocalDateTime startedAt;
    /** Timestamp when the job reached a terminal state (COMPLETED/FAILED) */
    private LocalDateTime completedAt;
}
