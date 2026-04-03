package com.instantchatbot.controller;

import com.instantchatbot.dto.request.CreateProjectRequest;
import com.instantchatbot.dto.response.ProjectResponse;
import com.instantchatbot.entity.User;
import com.instantchatbot.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing chatbot projects.
 * Provides endpoints for creating, listing, and retrieving project details.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Constructs the ProjectController with required services.
     *
     * @param projectService the service for project lifecycle management
     */
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * Creates a new project for the authenticated user.
     *
     * @param user the authenticated user principal
     * @param request details for the new project
     * @return the created project response DTO
     */
    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectResponse response = projectService.createProject(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all projects belonging to the authenticated user.
     *
     * @param user the authenticated user principal
     * @return a list of project responses
     */
    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @AuthenticationPrincipal User user) {
        List<ProjectResponse> projects = projectService.getUserProjects(user.getId());
        return ResponseEntity.ok(projects);
    }

    /**
     * Retrieves specific project details by identifier.
     *
     * @param user the authenticated user principal
     * @param id the unique identifier of the project
     * @return the project details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        ProjectResponse response = projectService.getProject(id, user.getId());
        return ResponseEntity.ok(response);
    }
}
