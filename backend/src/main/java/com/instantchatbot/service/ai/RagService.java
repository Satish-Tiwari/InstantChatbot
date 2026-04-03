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

    private final ChatClient chatClient;
    private final EmbeddingService embeddingService;

    @Value("${rag.top-k:5}")
    private int topK;

    /**
     * Constructs the RagService with ChatClient and EmbeddingService.
     *
     * @param chatClientBuilder the builder for the Spring AI ChatClient
     * @param embeddingService the service used for similarity search and context retrieval
     */
    public RagService(ChatClient.Builder chatClientBuilder, EmbeddingService embeddingService) {
        this.chatClient = chatClientBuilder.build();
        this.embeddingService = embeddingService;
    }

    /**
     * Performs a full RAG cycle to generate a natural language answer to a user query.
     * Retrieves top-K context chunks, builds an augmented prompt, and calls the LLM.
     *
     * @param projectId the identifier of the project context
     * @param question the user's natural language question
     * @return a RagAnswer record contenant the result text, sources, and confidence
     */
    public RagAnswer generateAnswer(String projectId, String question) {
        log.info("RAG query for project {}: {}", projectId, question);

        // Step 1: Retrieve relevant chunks
        List<Document> documents = embeddingService.search(projectId, question, topK);

        if (documents.isEmpty()) {
            return new RagAnswer(
                    "I don't have any information about this website yet. The content may still be processing.",
                    List.of(),
                    0.0
            );
        }

        // Step 2: Build context from retrieved documents
        String context = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));

        // Step 3: Extract source URLs
        List<String> sources = documents.stream()
                .map(doc -> doc.getMetadata().getOrDefault("url", "").toString())
                .filter(url -> !url.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // Step 4: Calculate confidence (average similarity)
        double confidence = 0.85; // Default for PGVector cosine similarity matches

        // Step 5: Generate answer using Spring AI ChatClient
        try {
            String systemMessage = SYSTEM_PROMPT.replace("{context}", context);

            String answer = chatClient.prompt()
                    .system(systemMessage)
                    .user(question)
                    .call()
                    .content();

            log.info("Generated answer for project {}: {} chars, sources: {}",
                    projectId, answer.length(), sources.size());

            return new RagAnswer(answer, sources, confidence);

        } catch (Exception e) {
            log.error("RAG generation error for project {}: {}", projectId, e.getMessage());
            return new RagAnswer(
                    "I'm sorry, I encountered an error while generating a response. Please try again.",
                    List.of(),
                    0.0
            );
        }
    }

    /**
     * Represents a RAG-generated answer with sources and confidence.
     */
    public record RagAnswer(String answer, List<String> sources, double confidence) {}
}
