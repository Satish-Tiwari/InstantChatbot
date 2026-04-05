package com.instantchatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a single execution of the web crawling and RAG processing pipeline.
 * Tracks granular progress of page discovery, processing, and chunking.
 */
@Entity
@Table(name = "crawl_jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrawlJob {

    /**
     * Unique identifier for the crawl job.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent project this job belongs to.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Counter for uniquely identified internal links found.
     */
    @Builder.Default
    private Integer pagesFound = 0;

    /**
     * Counter for pages successfully parsed and cleaned.
     */
    @Builder.Default
    private Integer pagesProcessed = 0;

    /**
     * Counter for semantic text chunks generated and stored.
     */
    @Builder.Default
    private Integer chunksCreated = 0;

    /**
     * Operational status of the job (e.g., QUEUED, IN_PROGRESS, COMPLETED, FAILED).
     */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CrawlStatus status = CrawlStatus.QUEUED;

    /**
     * Failure details if the status is set to FAILED.
     */
    private String errorMessage;

    /**
     * The URL currently being processed by the crawler.
     */
    private String currentUrl;

    /**
     * Timestamp when the job was initially queued or started.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime startedAt;

    /**
     * Timestamp when the job finished processing (successfully or otherwise).
     */
    private LocalDateTime completedAt;
}
