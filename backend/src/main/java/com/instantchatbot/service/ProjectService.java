package com.instantchatbot.service;

import com.instantchatbot.dto.request.CreateProjectRequest;
import com.instantchatbot.dto.response.CrawlJobResponse;
import com.instantchatbot.dto.response.ProjectResponse;
import com.instantchatbot.entity.*;
import com.instantchatbot.exception.ResourceNotFoundException;
import com.instantchatbot.repository.ProjectRepository;
import com.instantchatbot.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for managing chatbot projects.
 * Handles lifecycle operations such as creation, retrieval, and status/statistic updates.
 */
@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the ProjectService with required repositories.
     *
     * @param projectRepository the database repository for project entities
     * @param userRepository the database repository for user entities
     */
    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new chatbot project for a user.
     *
     * @param userId the identifier of the user owning the project
     * @param request metadata for the new project (name, URL)
     * @return the created project mapped to a response DTO
     * @throws ResourceNotFoundException if the user is not found
     */
    @Transactional
    public ProjectResponse createProject(Long userId, CreateProjectRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Project project = Project.builder()
                .name(request.getName())
                .websiteUrl(request.getWebsiteUrl())
                .user(user)
                .status(ProjectStatus.PENDING)
                .customAiProvider(request.getCustomAiProvider())
                .customOpenAiApiKey(request.getCustomOpenAiApiKey())
                .customAnthropicApiKey(request.getCustomAnthropicApiKey())
                .customGoogleProjectId(request.getCustomGoogleProjectId())
                .customGoogleLocation(request.getCustomGoogleLocation())
                .build();

        project = projectRepository.save(project);
        log.info("Project created: {} for user: {}", project.getId(), userId);

        return toResponse(project);
    }

    /**
     * Retrieves all projects belonging to a specific user.
     *
     * @param userId the identifier of the owner
     * @return a list of project response DTOs, ordered by creation date descending
     */
    public List<ProjectResponse> getUserProjects(Long userId) {
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a single project if it belongs to the authenticated user.
     *
     * @param projectId the identifier of the project
     * @param userId the identifier of the user requesting the project
     * @return the project mapped to a response DTO
     * @throws ResourceNotFoundException if the project does not exist for the given user
     */
    public ProjectResponse getProject(Long projectId, Long userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return toResponse(project);
    }

    /**
     * Updates the status of a project.
     *
     * @param projectId the identifier of the project to update
     * @param status the new lifecycle status to apply
     * @throws ResourceNotFoundException if the project is not found
     */
    @Transactional
    public void updateProjectStatus(Long projectId, ProjectStatus status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        project.setStatus(status);
        projectRepository.save(project);
        log.info("Project {} status updated to {}", projectId, status);
    }

    /**
     * Updates statistical counters for a project (e.g., after aSuccessful crawl).
     *
     * @param projectId the identifier of the project
     * @param pagesFound the total number of pages discovered
     * @param chunks the total number of text chunks created
     * @throws ResourceNotFoundException if the project is not found
     */
    @Transactional
    public void updateProjectStats(Long projectId, Integer pagesFound, Integer chunks) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        if (pagesFound != null) project.setPagesFound(pagesFound);
        if (chunks != null) project.setChunksCreated(chunks);
        projectRepository.save(project);
    }

    /**
     * Internal method to retrieve a project entity ensuring user ownership.
     *
     * @param projectId the identifier of the project
     * @param userId the identifier of the user
     * @return the raw Project entity
     */
    public Project getProjectEntity(Long projectId, Long userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }

    /**
     * Deletes a project and its associated data.
     *
     * @param projectId the identifier of the project 
     * @param userId the identifier of the owner
     * @throws ResourceNotFoundException if the project does not exist for the user
     */
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        
        projectRepository.delete(project);
        log.info("Project deleted: {} for user: {}", projectId, userId);
    }

    private ProjectResponse toResponse(Project project) {
        CrawlJobResponse crawlJobResponse = null;
        if (project.getCrawlJob() != null) {
            CrawlJob cj = project.getCrawlJob();
            crawlJobResponse = CrawlJobResponse.builder()
                    .id(cj.getId())
                    .pagesFound(cj.getPagesFound())
                    .pagesProcessed(cj.getPagesProcessed())
                    .chunksCreated(cj.getChunksCreated())
                    .status(cj.getStatus())
                    .errorMessage(cj.getErrorMessage())
                    .currentUrl(cj.getCurrentUrl())
                    .startedAt(cj.getStartedAt())
                    .completedAt(cj.getCompletedAt())
                    .build();
        }

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .websiteUrl(project.getWebsiteUrl())
                .status(project.getStatus())
                .pagesFound(project.getPagesFound())
                .chunksCreated(project.getChunksCreated())
                .crawlJob(crawlJobResponse)
                .downloadReady(project.getStatus() == ProjectStatus.READY)
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .customAiProvider(project.getCustomAiProvider())
                .hasCustomOpenAiKey(project.getCustomOpenAiApiKey() != null && !project.getCustomOpenAiApiKey().isBlank())
                .hasCustomAnthropicKey(project.getCustomAnthropicApiKey() != null && !project.getCustomAnthropicApiKey().isBlank())
                .hasCustomGoogleKey(project.getCustomGoogleProjectId() != null && !project.getCustomGoogleProjectId().isBlank())
                .build();
    }
}
