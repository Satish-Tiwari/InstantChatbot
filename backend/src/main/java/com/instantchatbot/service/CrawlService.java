package com.instantchatbot.service;

import com.instantchatbot.entity.*;
import com.instantchatbot.exception.BadRequestException;
import com.instantchatbot.exception.ResourceNotFoundException;
import com.instantchatbot.repository.CrawlJobRepository;
import com.instantchatbot.repository.ProjectRepository;
import com.instantchatbot.service.ai.CrawlPipelineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for initiating and managing web crawling jobs.
 * This service ensures a project is in a valid state for crawling and
 * triggers the asynchronous processing pipeline.
 */
@Service
public class CrawlService {

    private static final Logger log = LoggerFactory.getLogger(CrawlService.class);

    private final ProjectRepository projectRepository;
    private final CrawlJobRepository crawlJobRepository;
    private final CrawlPipelineService pipelineService;

    /**
     * Constructs the CrawlService with necessary components.
     *
     * @param projectRepository the repository for project management
     * @param crawlJobRepository the repository for tracking crawl jobs
     * @param pipelineService the orchestrator for the RAG processing pipeline
     */
    public CrawlService(ProjectRepository projectRepository,
                         CrawlJobRepository crawlJobRepository,
                         CrawlPipelineService pipelineService) {
        this.projectRepository = projectRepository;
        this.crawlJobRepository = crawlJobRepository;
        this.pipelineService = pipelineService;
    }

    /**
     * Initiates a crawl and RAG processing job for a specific project.
     */
    @Transactional
    public void startCrawl(Long projectId, Long userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Allow restarting from any state if not currently IN_PROGRESS
        CrawlJob currentJob = project.getCrawlJob();
        if (currentJob != null && currentJob.getStatus() == CrawlStatus.IN_PROGRESS) {
            throw new BadRequestException("Project is already being processed");
        }

        // Create or reset crawl job
        CrawlJob crawlJob = project.getCrawlJob();
        if (crawlJob == null) {
            crawlJob = CrawlJob.builder()
                    .project(project)
                    .status(CrawlStatus.QUEUED)
                    .build();
        } else {
            crawlJob.setStatus(CrawlStatus.QUEUED);
            crawlJob.setPagesFound(0);
            crawlJob.setPagesProcessed(0);
            crawlJob.setChunksCreated(0);
            crawlJob.setErrorMessage(null);
            crawlJob.setCompletedAt(null);
        }

        crawlJobRepository.save(crawlJob);
        project.setStatus(ProjectStatus.CRAWLING);
        projectRepository.save(project);

        log.info("Crawl started for project: {}", projectId);
        pipelineService.executePipeline(projectId);
    }

    /**
     * Attempts to stop a running crawl job after validating user ownership.
     *
     * @param projectId the unique identifier of the project 
     * @param userId the user ID to check for project ownership
     */
    @Transactional
    public void stopCrawl(Long projectId, Long userId) {
        validateOwnership(projectId, userId);
        pipelineService.stopJob(projectId);
    }

    /**
     * Attempts to pause a running crawl job after validating user ownership.
     *
     * @param projectId the unique identifier of the project 
     * @param userId the user ID to check for project ownership
     */
    @Transactional
    public void pauseCrawl(Long projectId, Long userId) {
        validateOwnership(projectId, userId);
        pipelineService.pauseJob(projectId);
    }

    /**
     * Attempts to resume a paused crawl job after validating user ownership.
     *
     * @param projectId the unique identifier of the project 
     * @param userId the user ID to check for project ownership
     */
    @Transactional
    public void resumeCrawl(Long projectId, Long userId) {
        validateOwnership(projectId, userId);
        pipelineService.resumeJob(projectId);
    }

    /**
     * Validates that a project exists and belongs to the specified user.
     *
     * @param projectId the unique identifier of the project
     * @param userId the identifier of the owner to validate
     * @throws ResourceNotFoundException if the project does not exist or is not owned by the user
     */
    private void validateOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ResourceNotFoundException("Project not found");
        }
    }
}
