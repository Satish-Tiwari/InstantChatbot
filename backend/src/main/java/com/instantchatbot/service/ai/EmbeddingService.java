package com.instantchatbot.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service facilitating vector embeddings management using Spring AI and PGVector.
 * Provides functionality for calculating embeddings, storing them in a vector database,
 * and performing similarity searches to retrieve relevant context.
 */
@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final VectorStore vectorStore;

    /**
     * Constructs the EmbeddingService with a pre-configured Spring AI VectorStore.
     *
     * @param vectorStore the underlying vector storage implementation (PGVector)
     */
    public EmbeddingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Generates embeddings for a set of text segments and stores them in the vector database.
     *
     * @param projectId unique identifier for the project scope (project isolation)
     * @param chunks the semantic segments generated from the website content
     * @return the total count of segments successfully embedded
     */
    public int embedChunks(String projectId, List<TextChunkerService.TextChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) return 0;

        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            TextChunkerService.TextChunk chunk = chunks.get(i);

            // Build metadata - Spring AI Document metadata must be String values
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("project_id", projectId);
            metadata.put("chunk_index", String.valueOf(i));

            // Copy chunk metadata
            if (chunk.metadata() != null) {
                for (Map.Entry<String, Object> entry : chunk.metadata().entrySet()) {
                    metadata.put(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }

            Document doc = new Document(chunk.text(), metadata);
            documents.add(doc);
        }

        // Spring AI VectorStore.add() automatically generates embeddings and stores them
        int batchSize = 50;
        int totalEmbedded = 0;

        for (int i = 0; i < documents.size(); i += batchSize) {
            int end = Math.min(i + batchSize, documents.size());
            List<Document> batch = documents.subList(i, end);

            try {
                vectorStore.add(batch);
                totalEmbedded += batch.size();
                log.info("Embedded batch {}: {} chunks for project {}",
                        (i / batchSize) + 1, batch.size(), projectId);
            } catch (Exception e) {
                log.error("Embedding error for batch {}: {}", (i / batchSize) + 1, e.getMessage());
            }
        }

        log.info("Total embedded: {}/{} chunks for project {}", totalEmbedded, chunks.size(), projectId);
        return totalEmbedded;
    }

    /**
     * Performs a cosine similarity search across stored embeddings within a project scope.
     *
     * @param projectId the project scope to narrow the search to
     * @param query the natural language query from the user
     * @param topK the maximum number of relevant documents to retrieve
     * @return a list of top-K relevant documents with their scores and metadata
     */
    public List<Document> search(String projectId, String query, int topK) {
        try {
            // Use Spring AI's built-in metadata filtering for project isolation
            SearchRequest request = SearchRequest.builder()
                    .query(query)
                    .topK(topK)
                    .filterExpression("project_id == '" + projectId + "'")
                    .build();

            List<Document> results = vectorStore.similaritySearch(request);

            log.info("Retrieved {} documents for project {}", results.size(), projectId);
            return results;

        } catch (Exception e) {
            log.error("Search error for project {}: {}", projectId, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Permanently removes all vector embeddings associated with a specific project.
     * Useful for re-crawling or project deletion to maintain clean index states.
     *
     * @param projectId the identifier of the project whose data should be purged
     */
    public void deleteProjectEmbeddings(String projectId) {
        try {
            // Search for all docs with this project_id and delete them
            SearchRequest request = SearchRequest.builder()
                    .query("*")
                    .topK(1000)
                    .filterExpression("project_id == '" + projectId + "'")
                    .build();

            List<Document> docs = vectorStore.similaritySearch(request);
            if (!docs.isEmpty()) {
                List<String> ids = docs.stream()
                        .map(Document::getId)
                        .collect(Collectors.toList());
                vectorStore.delete(ids);
                log.info("Deleted {} embeddings for project {}", ids.size(), projectId);
            }
        } catch (Exception e) {
            log.warn("Could not delete embeddings for project {}: {}", projectId, e.getMessage());
        }
    }
}
