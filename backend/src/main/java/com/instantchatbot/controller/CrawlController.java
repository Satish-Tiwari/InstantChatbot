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
 * Provides endpoints for initiating a new crawl, monitoring current progress, 
 * and controlling job lifecycle (stop, pause, resume).
 */
@RestController
@RequestMapping("/api/projects/{projectId}")
public class CrawlController {

    private final CrawlService crawlService;
    private final ProjectService projectService;

    /**
     * Constructs the controller with required crawl and project services.
     */
    public CrawlController(CrawlService crawlService, ProjectService projectService) {
        this.crawlService = crawlService;
        this.projectService = projectService;
    }

    /**
     * Starts a new web crawl and RAG processing pipeline for a project.
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
     * Stops a running crawl job for the specified project.
     *
     * @param user the authenticated user principal
     * @param projectId the project identifier 
     * @return a message confirming the stop request
     */
    @PostMapping("/stop")
    public ResponseEntity<Map<String, String>> stopCrawl(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId) {
        crawlService.stopCrawl(projectId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Stopping crawl..."));
    }

    /**
     * Pauses an active crawl job for the specified project.
     *
     * @param user the authenticated user principal
     * @param projectId the project identifier 
     * @return a message confirming the pause request
     */
    @PostMapping("/pause")
    public ResponseEntity<Map<String, String>> pauseCrawl(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId) {
        crawlService.pauseCrawl(projectId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Crawl paused"));
    }

    /**
     * Resumes a paused crawl job for the specified project.
     *
     * @param user the authenticated user principal
     * @param projectId the project identifier 
     * @return a message confirming the resume request
     */
    @PostMapping("/resume")
    public ResponseEntity<Map<String, String>> resumeCrawl(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId) {
        crawlService.resumeCrawl(projectId, user.getId());
        return ResponseEntity.ok(Map.of("message", "Resuming crawl..."));
    }

    /**
     * Retrieves the current processing status and crawl statistics for a project.
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId, user.getId()));
    }
}
