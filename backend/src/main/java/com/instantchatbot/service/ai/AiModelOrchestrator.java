package com.instantchatbot.service.ai;

import com.instantchatbot.entity.Project;
import com.instantchatbot.repository.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.anthropic.api.AnthropicApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for orchestrating multiple AI providers with failover support.
 * It manages both global admin keys and project-specific user keys.
 */
@Service
public class AiModelOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(AiModelOrchestrator.class);

    private final ProjectRepository projectRepository;

    @Value("${spring.ai.openai.api-key:}")
    private String globalOpenAiKey;

    @Value("${spring.ai.anthropic.api-key:}")
    private String globalAnthropicKey;

    @Value("${spring.ai.vertex.ai.gemini.project-id:}")
    private String globalGoogleProject;

    @Value("${spring.ai.vertex.ai.gemini.location:us-central1}")
    private String globalGoogleLocation;

    /**
     * Constructs the orchestrator with required repository.
     * @param projectRepository repository to fetch project-specific AI settings
     */
    public AiModelOrchestrator(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /**
     * Retrieves a prioritized list of available ChatModels for a project.
     * Logic: User Keys (OpenAI -> Anthropic) followed by Global Keys.
     * 
     * @param projectId the identifier of the project requesting AI generation
     * @return a list of initialized ChatModel instances ready for failover loop
     */
    public List<ChatModel> getAvailableModelsForProject(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        List<ChatModel> models = new ArrayList<>();

        if (project == null) return models;

        // 1. Try User-Specific Keys First
        addCustomModels(project, models);

        // 2. Add Global Models as Fallback
        addGlobalModels(models);

        return models;
    }

    /**
     * Checks for a user-provided OpenAI embedding model.
     * Note: Currently only supports custom OpenAI embedding.
     * 
     * @param projectId the project identifier
     * @return an Optional containing the custom EmbeddingModel if key is provided
     */
    public Optional<org.springframework.ai.embedding.EmbeddingModel> getEmbeddingModelForProject(Long projectId) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project != null && isNotBlank(project.getCustomOpenAiApiKey())) {
            log.info("Using custom OpenAI embedding model for project {}", projectId);
            return Optional.of(new org.springframework.ai.openai.OpenAiEmbeddingModel(
                OpenAiApi.builder().apiKey(project.getCustomOpenAiApiKey()).build()));
        }
        return Optional.empty(); // Fallback to global will be handled by the service
    }

    /**
     * Resolves and adds custom user-provided ChatModels to the priority list.
     */
    private void addCustomModels(Project project, List<ChatModel> models) {
        if (isNotBlank(project.getCustomOpenAiApiKey())) {
            log.info("Adding custom OpenAI model for project {}", project.getId());
            models.add(new OpenAiChatModel(OpenAiApi.builder().apiKey(project.getCustomOpenAiApiKey()).build()));
        }
        if (isNotBlank(project.getCustomAnthropicApiKey())) {
            log.info("Adding custom Anthropic model for project {}", project.getId());
            models.add(new AnthropicChatModel(new AnthropicApi(project.getCustomAnthropicApiKey())));
        }
        if (isNotBlank(project.getCustomGoogleProjectId())) {
            log.info("Adding custom Google model for project {}", project.getId());
            // VertexAI setup often requires more than just ID/Location (Service Account), 
            // but we'll implement the placeholder for consistency.
        }
    }

    /**
     * Adds global admin-provided ChatModels as a final failover option.
     */
    private void addGlobalModels(List<ChatModel> models) {
        if (isNotBlank(globalOpenAiKey)) {
            models.add(new OpenAiChatModel(OpenAiApi.builder().apiKey(globalOpenAiKey).build()));
        }
        if (isNotBlank(globalAnthropicKey)) {
            models.add(new AnthropicChatModel(new AnthropicApi(globalAnthropicKey)));
        }
    }

    /**
     * Helper to check if a string is not null or whitespace.
     */
    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
