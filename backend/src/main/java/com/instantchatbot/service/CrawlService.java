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
     * Resets or creates a new CrawlJob record and triggers the async pipeline.
     *
     * @param projectId the unique identifier of the project to crawl
     * @param userId the identifier of the requesting user (for ownership validation)
     * @throws ResourceNotFoundException if the project does not exist
     * @throws BadRequestException if the project is currently being processed
     */
    @Transactional
    public void startCrawl(Long projectId, Long userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        if (project.getStatus() != ProjectStatus.PENDING &&
            project.getStatus() != ProjectStatus.FAILED) {
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

        // Trigger async pipeline — everything runs inside Spring Boot now
        pipelineService.executePipeline(projectId);
    }
}
