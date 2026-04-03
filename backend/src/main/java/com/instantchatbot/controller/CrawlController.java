package com.instantchatbot.controller;

import com.instantchatbot.entity.User;
import com.instantchatbot.service.CrawlService;
import com.instantchatbot.service.ProjectService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for managing project-specific web crawling tasks.
 * Provides endpoints for initiating a new crawl and monitoring current progress.
 */
@RestController
@RequestMapping("/api/projects/{projectId}")
public class CrawlController {

    private final CrawlService crawlService;
    private final ProjectService projectService;

    /**
     * Constructs the controller with required crawl and project services.
     *
     * @param crawlService the service that handles crawl initiation
     * @param projectService the service used for status lookups
     */
    public CrawlController(CrawlService crawlService, ProjectService projectService) {
        this.crawlService = crawlService;
        this.projectService = projectService;
    }

    /**
     * Starts a new web crawl and RAG processing pipeline for a project.
     *
     * @param user the authenticated user principal
     * @param projectId the identifier of the project to crawl
     * @return a message confirming the start of the job
     */
    @PostMapping("/crawl")
    public ResponseEntity<Map<String, String>> startCrawl(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId) {
        crawlService.startCrawl(projectId, user.getId());
        return ResponseEntity.ok(Map.of(
                "message", "Crawl started",
                "projectId", projectId.toString()
        ));
    }

    /**
     * Retrieves the current processing status and crawl statistics for a project.
     *
     * @param user the authenticated user principal
     * @param projectId the identifier of the project
     * @return the project details containing current crawl job status
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId, user.getId()));
    }
}
