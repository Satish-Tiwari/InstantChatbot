package com.instantchatbot.service.ai;

import com.instantchatbot.entity.*;
import com.instantchatbot.exception.ResourceNotFoundException;
import com.instantchatbot.repository.CrawlJobRepository;
import com.instantchatbot.repository.ProjectRepository;
import com.instantchatbot.service.ai.ContentCleanerService.CleanedContent;
import com.instantchatbot.service.ai.TextChunkerService.TextChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.instantchatbot.dto.response.CrawlProgressUpdate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrator service for the Retrieval-Augmented Generation (RAG) pipeline.
 * Coordinates the full flow: Web Crawling -> Content Cleaning -> Text Chunking -> Vector Embedding.
 * Each step is executed sequentially for a project, moving its lifecycle status forward.
 */
@Service
public class CrawlPipelineService {

    private static final Logger log = LoggerFactory.getLogger(CrawlPipelineService.class);

    private final ProjectRepository projectRepository;
    private final CrawlJobRepository crawlJobRepository;
    private final WebCrawlerService crawlerService;
    private final ContentCleanerService cleanerService;
    private final TextChunkerService chunkerService;
    private final EmbeddingService embeddingService;
    private final SimpMessagingTemplate messagingTemplate;

    private final Map<Long, Boolean> stopFlags = new ConcurrentHashMap<>();
    private final Map<Long, Boolean> pauseFlags = new ConcurrentHashMap<>();

    /**
     * Constructs the CrawlPipelineService with all required AI sub-services.
     *
     * @param projectRepository the repository for project metadata
     * @param crawlJobRepository the repository for tracking progress
     * @param crawlerService the service for discovering and downloading web pages
     * @param cleanerService the service for stripping HTML noise and boilerplate
     * @param chunkerService the service for splitting text into semantic units
     * @param embeddingService the service for generating and storing vector representations
     */
    public CrawlPipelineService(ProjectRepository projectRepository,
                                 CrawlJobRepository crawlJobRepository,
                                 WebCrawlerService crawlerService,
                                 ContentCleanerService cleanerService,
                                 TextChunkerService chunkerService,
                                 EmbeddingService embeddingService,
                                 SimpMessagingTemplate messagingTemplate) {
        this.projectRepository = projectRepository;
        this.crawlJobRepository = crawlJobRepository;
        this.crawlerService = crawlerService;
        this.cleanerService = cleanerService;
        this.chunkerService = chunkerService;
        this.embeddingService = embeddingService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Asynchronously executes the full RAG pipeline for a given project identifier.
     * This method is the entry point for background project processing.
     *
     * @param projectId the identifier of the project to process
     */
    @Async
    public void executePipeline(Long projectId) {
        log.info("Starting full pipeline for project: {}", projectId);

        try {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

            // --- STEP 1: CRAWL ---
            updateStatus(projectId, ProjectStatus.CRAWLING, CrawlStatus.IN_PROGRESS);

            List<WebCrawlerService.CrawledPage> pages = crawlerService.crawl(
                    project.getWebsiteUrl(), 50, 3,
                    (url) -> updateCrawlProgress(projectId, url),
                    () -> {
                        checkPaused(projectId);
                        return !Boolean.TRUE.equals(stopFlags.get(projectId));
                    }
            );

            if (Boolean.TRUE.equals(stopFlags.get(projectId))) {
                handleStop(projectId);
                return;
            }

            updateCrawlStats(projectId, pages.size(), 0, 0);

            if (pages.isEmpty()) {
                failPipeline(projectId, "No pages could be crawled from the provided URL");
                return;
            }

            // --- STEP 2: CLEAN ---
            updateStatus(projectId, ProjectStatus.PROCESSING, CrawlStatus.IN_PROGRESS);

            List<CleanedContent> cleanedPages = new ArrayList<>();
            for (WebCrawlerService.CrawledPage page : pages) {
                CleanedContent cleaned = cleanerService.clean(page.html(), page.url());
                if (cleaned.text() != null && cleaned.text().length() > 50) {
                    cleanedPages.add(cleaned);
                }
            }

            log.info("Cleaned {}/{} pages for project {}", cleanedPages.size(), pages.size(), projectId);
            updateCrawlStats(projectId, pages.size(), cleanedPages.size(), 0);

            // --- STEP 3: CHUNK ---
            List<TextChunk> allChunks = new ArrayList<>();
            for (CleanedContent page : cleanedPages) {
                Map<String, Object> metadata = Map.of(
                        "url", page.url(),
                        "title", page.title() != null ? page.title() : ""
                );
                List<TextChunk> pageChunks = chunkerService.chunkText(page.text(), metadata);
                allChunks.addAll(pageChunks);
            }

            log.info("Created {} chunks from {} pages for project {}",
                    allChunks.size(), cleanedPages.size(), projectId);

            // --- STEP 4: EMBED ---
            updateStatus(projectId, ProjectStatus.EMBEDDING, CrawlStatus.IN_PROGRESS);

            // Delete existing embeddings for this project (in case of re-crawl)
            embeddingService.deleteProjectEmbeddings(projectId.toString());

            int chunksEmbedded = embeddingService.embedChunks(projectId.toString(), allChunks);

            updateCrawlStats(projectId, pages.size(), cleanedPages.size(), chunksEmbedded);

            // --- STEP 5: COMPLETE ---
            completePipeline(projectId, pages.size(), chunksEmbedded);

            log.info("Pipeline complete for project {}: {} pages, {} chunks embedded",
                    projectId, pages.size(), chunksEmbedded);

        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "unknown error";
            log.error("Pipeline failed for project {}: {}", projectId, errorMsg, e);
            
            String userFriendlyError;
            if (errorMsg.contains("quota") || errorMsg.contains("billing") || errorMsg.contains("429")) {
                Project p = projectRepository.findById(projectId).orElse(null);
                boolean hasUserKey = p != null && (p.getCustomOpenAiApiKey() != null || p.getCustomAnthropicApiKey() != null);
                
                if (hasUserKey) {
                    userFriendlyError = "Your provided AI Key has exceeded its quota or billing limit. Please check your AI provider console.";
                } else {
                    userFriendlyError = "Global AI quota exceeded. Admin has been notified, or you can provide your own API key in project settings to proceed.";
                }
            } else {
                userFriendlyError = "An unexpected error occurred during processing: " + (e.getMessage() != null ? e.getMessage() : "Check logs for details");
            }
            
            failPipeline(projectId, userFriendlyError);
        }
    }

    @Transactional
    protected void updateStatus(Long projectId, ProjectStatus projectStatus, CrawlStatus crawlStatus) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            project.setStatus(projectStatus);
            projectRepository.save(project);
        }

        CrawlJob job = crawlJobRepository.findByProjectId(projectId).orElse(null);
        if (job != null) {
            job.setStatus(crawlStatus);
            crawlJobRepository.save(job);
            
            broadcastUpdate(projectId, projectStatus, crawlStatus, 
                    nullToZero(job.getPagesFound()), 
                    nullToZero(job.getPagesProcessed()), 
                    nullToZero(job.getChunksCreated()), 
                    job.getCurrentUrl());
        }
    }

    @Transactional
    protected void updateCrawlProgress(Long projectId, String currentUrl) {
        CrawlJob job = crawlJobRepository.findByProjectId(projectId).orElse(null);
        if (job != null) {
            job.setCurrentUrl(currentUrl);
            
            // Increment pages found as we discover new URLs
            int currentFound = nullToZero(job.getPagesFound());
            job.setPagesFound(currentFound + 1);
            
            crawlJobRepository.save(job);
            
            // Log for debugging
            log.debug("[{}] Progress update: Found={}, URL={}", 
                    projectId, job.getPagesFound(), currentUrl);

            broadcastUpdate(projectId, ProjectStatus.CRAWLING, job.getStatus(), 
                    job.getPagesFound(), 
                    nullToZero(job.getPagesProcessed()), 
                    nullToZero(job.getChunksCreated()), currentUrl);
        }
    }

    @Transactional
    protected void updateCrawlStats(Long projectId, int pagesFound, int pagesProcessed, int chunks) {
        CrawlJob job = crawlJobRepository.findByProjectId(projectId).orElse(null);
        String currentUrl = null;
        CrawlStatus crawlStatus = CrawlStatus.IN_PROGRESS;
        
        if (job != null) {
            job.setPagesFound(pagesFound);
            job.setPagesProcessed(pagesProcessed);
            job.setChunksCreated(chunks);
            crawlJobRepository.save(job);
            currentUrl = job.getCurrentUrl();
            crawlStatus = job.getStatus();
        }

        Project project = projectRepository.findById(projectId).orElse(null);
        ProjectStatus projectStatus = ProjectStatus.CRAWLING;
        if (project != null) {
            project.setPagesFound(pagesFound);
            project.setChunksCreated(chunks);
            projectRepository.save(project);
            projectStatus = project.getStatus();
        }
        
        broadcastUpdate(projectId, projectStatus, crawlStatus, pagesFound, pagesProcessed, chunks, currentUrl);
    }

    @Transactional
    protected void completePipeline(Long projectId, int pages, int chunks) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            project.setStatus(ProjectStatus.READY);
            project.setPagesFound(pages);
            project.setChunksCreated(chunks);
            projectRepository.save(project);
        }

        CrawlJob job = crawlJobRepository.findByProjectId(projectId).orElse(null);
        if (job != null) {
            job.setStatus(CrawlStatus.COMPLETED);
            job.setPagesFound(pages);
            job.setPagesProcessed(pages);
            job.setChunksCreated(chunks);
            job.setCompletedAt(LocalDateTime.now());
            crawlJobRepository.save(job);
            
            broadcastUpdate(projectId, ProjectStatus.READY, CrawlStatus.COMPLETED, 
                    pages, pages, chunks, job.getCurrentUrl());
        }
    }

    @Transactional
    protected void failPipeline(Long projectId, String errorMessage) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            project.setStatus(ProjectStatus.FAILED);
            projectRepository.save(project);
        }

        CrawlJob job = crawlJobRepository.findByProjectId(projectId).orElse(null);
        if (job != null) {
            job.setStatus(CrawlStatus.FAILED);
            job.setErrorMessage(errorMessage);
            job.setCompletedAt(LocalDateTime.now());
            crawlJobRepository.save(job);
            
            broadcastUpdate(projectId, ProjectStatus.FAILED, CrawlStatus.FAILED, 
                    job.getPagesFound(), job.getPagesProcessed(), job.getChunksCreated(), job.getCurrentUrl());
        }
    }

    private void broadcastUpdate(Long projectId, ProjectStatus pStatus, CrawlStatus cStatus, 
                                 int pagesFound, int pagesProcessed, int chunks, String url) {
        
        log.info("[{}] Broadcaster: Counter={} | URL={}", 
                projectId, pagesFound, url != null ? url : "INITIALIZING");

        CrawlProgressUpdate update = CrawlProgressUpdate.builder()
                .projectId(projectId)
                .projectStatus(pStatus)
                .crawlStatus(cStatus)
                .pagesFound(pagesFound)
                .pagesProcessed(pagesProcessed)
                .chunksCreated(chunks)
                .currentUrl(url)
                .build();

        messagingTemplate.convertAndSend("/topic/project/" + projectId, update);
    }

    private int nullToZero(Integer value) {
        return value == null ? 0 : value;
    }

    /**
     * Signals a crawl job to stop execution as soon as possible.
     * Clears any active pause flags to ensure the job can terminate.
     *
     * @param projectId the unique identifier of the project's background job
     */
    public void stopJob(Long projectId) {
        stopFlags.put(projectId, true);
        pauseFlags.remove(projectId); // Unpause if paused so it can stop
        log.info("Stop requested for project {}", projectId);
    }

    /**
     * Pauses an active crawl job by setting a thread-safe flag.
     * Updates the project status to PAUSED and notifies the frontend.
     *
     * @param projectId the unique identifier of the project's background job
     */
    public void pauseJob(Long projectId) {
        pauseFlags.put(projectId, true);
        updateStatus(projectId, ProjectStatus.CRAWLING, CrawlStatus.PAUSED);
        log.info("Pause requested for project {}", projectId);
    }

    /**
     * Resumes a paused crawl job or restarts a stopped one.
     * If the job was paused, it unsets the flag. Otherwise, it restarts the pipeline.
     *
     * @param projectId the unique identifier of the project's background job
     */
    public void resumeJob(Long projectId) {
        if (Boolean.TRUE.equals(pauseFlags.get(projectId))) {
            pauseFlags.remove(projectId);
            updateStatus(projectId, ProjectStatus.CRAWLING, CrawlStatus.IN_PROGRESS);
            log.info("Resume requested for project {}", projectId);
        } else {
            // If it wasn't paused, maybe it was failed/stopped, so restart
            executePipeline(projectId);
        }
    }

    /**
     * Blocking checkpoint that waits while a project's pause flag is active.
     * Used by the pipeline processing loops to check for pause signals.
     *
     * @param projectId the project identifier to check for a pause signal
     */
    private void checkPaused(Long projectId) {
        while (Boolean.TRUE.equals(pauseFlags.get(projectId))) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Finalizes the state of a stopped crawl job.
     * Clears all flags and marks the job as CANCELLED.
     *
     * @param projectId the unique identifier of the project
     */
    private void handleStop(Long projectId) {
        stopFlags.remove(projectId);
        pauseFlags.remove(projectId);
        updateStatus(projectId, ProjectStatus.FAILED, CrawlStatus.CANCELLED);
        log.info("Job stopped for project {}", projectId);
    }
}
