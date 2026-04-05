package com.instantchatbot.controller;

import com.instantchatbot.dto.request.ChatRequest;
import com.instantchatbot.dto.response.ChatResponse;
import com.instantchatbot.entity.Project;
import com.instantchatbot.entity.ProjectStatus;
import com.instantchatbot.entity.User;
import com.instantchatbot.exception.BadRequestException;
import com.instantchatbot.service.CodeGeneratorService;
import com.instantchatbot.service.ProjectService;
import com.instantchatbot.service.ai.RagService;
import com.instantchatbot.service.ai.RagService.RagAnswer;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

/**
 * REST controller for interactive chatbot communication and package distribution.
 * Integrates the RAG pipeline for real-time chat and the generation service for downloads.
 */
@RestController
@RequestMapping("/api/projects/{projectId}")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ProjectService projectService;
    private final CodeGeneratorService codeGeneratorService;
    private final RagService ragService;

    /**
     * Constructs the ChatController with required dependencies.
     *
     * @param projectService service for project metadata
     * @param codeGeneratorService service for ZIP package generation
     * @param ragService service for context-aware chat generation
     */
    public ChatController(ProjectService projectService,
                           CodeGeneratorService codeGeneratorService,
                           RagService ragService) {
        this.projectService = projectService;
        this.codeGeneratorService = codeGeneratorService;
        this.ragService = ragService;
    }

    /**
     * Handles an interactive chat message for a specific project.
     * Uses the RAG pipeline to generate a response from the project's crawled content.
     *
     * @param user the authenticated user principal
     * @param projectId the identifier of the project to query
     * @param request the user's message
     * @return the AI-generated answer with source citations
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId,
            @Valid @RequestBody ChatRequest request) {

        Project project = projectService.getProjectEntity(projectId, user.getId());

        if (project.getStatus() != ProjectStatus.READY) {
            throw new BadRequestException(
                    "Project is not ready for chat. Current status: " + project.getStatus());
        }

        log.info("Chat request for project {}: {}", projectId, request.getMessage());

        // Use Spring AI RagService directly - no external HTTP call
        RagAnswer ragAnswer = ragService.generateAnswer(
                projectId.toString(), request.getMessage());

        return ResponseEntity.ok(ChatResponse.builder()
                .answer(ragAnswer.answer())
                .sources(ragAnswer.sources())
                .confidence(ragAnswer.confidence())
                .build());
    }

    /**
     * Generates and serves a standalone chatbot package for download.
     *
     * @param user the authenticated user principal
     * @param projectId the identifier of the project to package
     * @return the generated ZIP file as an octet-stream
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadBot(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId) {

        Project project = projectService.getProjectEntity(projectId, user.getId());

        if (project.getStatus() != ProjectStatus.READY) {
            throw new BadRequestException("Chatbot is not ready for download");
        }

        Path zipPath = codeGeneratorService.getZipPath(projectId);
        Resource resource = new FileSystemResource(zipPath);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"chatbot-" + projectId + ".zip\"")
                .body(resource);
    }
}
