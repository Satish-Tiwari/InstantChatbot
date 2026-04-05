package com.instantchatbot.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Primary service for Retrieval-Augmented Generation (RAG) orchestration.
 * It combines vector search retrieval with large language model generation
 * to provide context-aware answers derived from project-specific content.
 */
@Service
public class RagService {

    private static final Logger log = LoggerFactory.getLogger(RagService.class);

    private static final String SYSTEM_PROMPT = """
            You are a helpful AI assistant that answers questions based on the provided website content.
            
            Rules:
            1. ONLY answer based on the provided context. Do not use external knowledge.
            2. If the answer is not found in the context, say: "I don't have enough information from the website to answer that question."
            3. Be concise but thorough in your answers.
            4. If relevant, mention which part of the website the information comes from.
            5. Use a friendly, professional tone.
            
            Context from the website:
            {context}
            """;

    private final AiModelOrchestrator modelOrchestrator;
    private final EmbeddingService embeddingService;

    @Value("${rag.top-k:5}")
    private int topK;

    public RagService(AiModelOrchestrator modelOrchestrator, EmbeddingService embeddingService) {
        this.modelOrchestrator = modelOrchestrator;
        this.embeddingService = embeddingService;
    }

    /**
     * Performs a full RAG cycle to generate a natural language answer to a user query.
     * Retrieves top-K context chunks, builds an augmented prompt, and calls the LLM.
     * Automatically fails over to alternate providers if quota errors occur.
     *
     * @param projectIdStr the identifier of the project context (as String)
     * @param question the user's natural language question
     * @return a RagAnswer record containing the result text, sources, and confidence
     */
    public RagAnswer generateAnswer(String projectIdStr, String question) {
        Long projectId = Long.parseLong(projectIdStr);
        log.info("RAG query for project {}: {}", projectId, question);

        // Step 1: Retrieve relevant chunks
        List<Document> documents = embeddingService.search(projectIdStr, question, topK);
        if (documents.isEmpty()) {
            return new RagAnswer("I don't have any info for this website. Content may be still processing.", List.of(), 0.0);
        }

        // Step 2: Build context
        String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n\n---\n\n"));
        List<String> sources = documents.stream()
                .map(doc -> doc.getMetadata().getOrDefault("url", "").toString())
                .filter(url -> !url.isEmpty()).distinct().collect(Collectors.toList());

        // Step 3: Get available models (failover list)
        List<org.springframework.ai.chat.model.ChatModel> models = modelOrchestrator.getAvailableModelsForProject(projectId);
        
        if (models.isEmpty()) {
            return new RagAnswer("No AI providers configured. Please add your API keys.", List.of(), 0.0);
        }

        // Step 4: Try models with failover
        String systemMessage = SYSTEM_PROMPT.replace("{context}", context);

        for (int i = 0; i < models.size(); i++) {
            org.springframework.ai.chat.model.ChatModel model = models.get(i);
            try {
                ChatClient client = ChatClient.builder(model).build();
                String answer = client.prompt()
                        .system(systemMessage)
                        .user(question)
                        .call()
                        .content();

                return new RagAnswer(answer, sources, 0.85);

            } catch (Exception e) {
                String errorMsg = e.getMessage().toLowerCase();
                boolean isQuotaError = errorMsg.contains("quota") || errorMsg.contains("429") || errorMsg.contains("billing");

                if (isQuotaError && i < models.size() - 1) {
                    log.warn("Quota exceeded for provider {}. Failing over to next available...", i);
                    continue; // Try next model
                } else {
                    log.error("RAG generation failed for project {}: {}", projectId, e.getMessage());
                    String userMsg = isQuotaError 
                        ? "API quota exceeded and no fallback models available. Please contact admin or add your own key."
                        : "Error generating response. Please try again later.";
                    return new RagAnswer(userMsg, List.of(), 0.0);
                }
            }
        }

        return new RagAnswer("System error: No models worked.", List.of(), 0.0);
    }

    /**
     * Represents a RAG-generated answer with sources and confidence.
     */
    public record RagAnswer(String answer, List<String> sources, double confidence) {}
}
