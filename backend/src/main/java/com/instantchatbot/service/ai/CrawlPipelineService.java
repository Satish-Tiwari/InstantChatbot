package com.instantchatbot.service.ai;

import com.instantchatbot.entity.*;
import com.instantchatbot.exception.ResourceNotFoundException;
import com.instantchatbot.repository.CrawlJobRepository;
import com.instantchatbot.repository.ProjectRepository;
import com.instantchatbot.service.ai.ContentCleanerService.CleanedContent;
import com.instantchatbot.service.ai.TextChunkerService.TextChunk;
import com.instantchatbot.service.ai.WebCrawlerService.CrawledPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                                 EmbeddingService embeddingService) {
        this.projectRepository = projectRepository;
        this.crawlJobRepository = crawlJobRepository;
        this.crawlerService = crawlerService;
        this.cleanerService = cleanerService;
        this.chunkerService = chunkerService;
        this.embeddingService = embeddingService;
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

            // ═══ STEP 1: CRAWL ═══
            updateStatus(projectId, ProjectStatus.CRAWLING, CrawlStatus.IN_PROGRESS);

            List<CrawledPage> pages = crawlerService.crawl(
                    project.getWebsiteUrl(), 50, 3);

            updateCrawlStats(projectId, pages.size(), 0, 0);

            if (pages.isEmpty()) {
                failPipeline(projectId, "No pages could be crawled from the provided URL");
                return;
            }

            // ═══ STEP 2: CLEAN ═══
            updateStatus(projectId, ProjectStatus.PROCESSING, CrawlStatus.IN_PROGRESS);

            List<CleanedContent> cleanedPages = new ArrayList<>();
            for (CrawledPage page : pages) {
                CleanedContent cleaned = cleanerService.clean(page.html(), page.url());
                if (cleaned.text() != null && cleaned.text().length() > 50) {
                    cleanedPages.add(cleaned);
                }
            }

            log.info("Cleaned {}/{} pages for project {}", cleanedPages.size(), pages.size(), projectId);
            updateCrawlStats(projectId, pages.size(), cleanedPages.size(), 0);

            // ═══ STEP 3: CHUNK ═══
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

            // ═══ STEP 4: EMBED ═══
            updateStatus(projectId, ProjectStatus.EMBEDDING, CrawlStatus.IN_PROGRESS);

            // Delete existing embeddings for this project (in case of re-crawl)
            embeddingService.deleteProjectEmbeddings(projectId.toString());

            int chunksEmbedded = embeddingService.embedChunks(projectId.toString(), allChunks);

            updateCrawlStats(projectId, pages.size(), cleanedPages.size(), chunksEmbedded);

            // ═══ STEP 5: COMPLETE ═══
            completePipeline(projectId, pages.size(), chunksEmbedded);

            log.info("Pipeline complete for project {}: {} pages, {} chunks embedded",
                    projectId, pages.size(), chunksEmbedded);

        } catch (Exception e) {
            log.error("Pipeline failed for project {}: {}", projectId, e.getMessage(), e);
            failPipeline(projectId, e.getMessage());
        }
    }

    @Transactional
    protected void updateStatus(Long projectId, ProjectStatus projectStatus, CrawlStatus crawlStatus) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            project.setStatus(projectStatus);
            projectRepository.save(project);
        }

        CrawlJob crawlJob = crawlJobRepository.findByProjectId(projectId).orElse(null);
        if (crawlJob != null) {
            crawlJob.setStatus(crawlStatus);
            crawlJobRepository.save(crawlJob);
        }
    }

    @Transactional
    protected void updateCrawlStats(Long projectId, int pagesFound, int pagesProcessed, int chunks) {
        CrawlJob job = crawlJobRepository.findByProjectId(projectId).orElse(null);
        if (job != null) {
            job.setPagesFound(pagesFound);
            job.setPagesProcessed(pagesProcessed);
            job.setChunksCreated(chunks);
            crawlJobRepository.save(job);
        }

        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null) {
            project.setPagesFound(pagesFound);
            project.setChunksCreated(chunks);
            projectRepository.save(project);
        }
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
        }
    }
}
