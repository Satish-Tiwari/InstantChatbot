package com.instantchatbot.entity;

/**
 * Defines the operational status of a specific crawling and processing job.
 */
public enum CrawlStatus {
    QUEUED,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
