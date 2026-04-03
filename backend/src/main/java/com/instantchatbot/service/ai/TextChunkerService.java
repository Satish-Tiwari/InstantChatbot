package com.instantchatbot.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service providing text segmentation for Retrieval-Augmented Generation.
 * It implements a strategy for splitting large text blocks into smaller, overlapping
 * semantic chunks to ensure efficient vector search and contextual relevance.
 */
@Service
public class TextChunkerService {

    private static final Logger log = LoggerFactory.getLogger(TextChunkerService.class);

    @Value("${rag.chunk-size:500}")
    private int chunkSize; // target tokens (approximated as words * 1.3)

    @Value("${rag.chunk-overlap:50}")
    private int chunkOverlap;

    /**
     * Splits a given text into a list of semantic chunks based on word count and overlap.
     * It handles paragraph splits and large block segmentation using sentence boundaries.
     *
     * @param text the raw source text to be chunked
     * @param metadata base metadata to be attached to each generated chunk
     * @return a list of generated TextChunk records containing segment data and metadata
     */
    public List<TextChunk> chunkText(String text, Map<String, Object> metadata) {
        if (text == null || text.trim().length() < 30) {
            return Collections.emptyList();
        }

        List<TextChunk> chunks = new ArrayList<>();

        // Split into paragraphs first
        List<String> paragraphs = splitParagraphs(text);

        List<String> currentParts = new ArrayList<>();
        int currentWordCount = 0;

        for (String para : paragraphs) {
            int paraWords = countWords(para);

            // If a single paragraph is too large, split by sentences
            if (paraWords > chunkSize) {
                // Flush current buffer
                if (!currentParts.isEmpty()) {
                    chunks.add(createChunk(String.join("\n\n", currentParts), metadata, chunks.size()));
                    currentParts = getOverlapParts(currentParts);
                    currentWordCount = currentParts.stream().mapToInt(this::countWords).sum();
                }
                // Split large paragraph
                List<String> sentenceChunks = splitBySentences(para);
                for (String sc : sentenceChunks) {
                    chunks.add(createChunk(sc, metadata, chunks.size()));
                }
                continue;
            }

            // Check if adding this paragraph exceeds the limit
            if (currentWordCount + paraWords > chunkSize && !currentParts.isEmpty()) {
                chunks.add(createChunk(String.join("\n\n", currentParts), metadata, chunks.size()));

                // Keep overlap
                currentParts = getOverlapParts(currentParts);
                currentWordCount = currentParts.stream().mapToInt(this::countWords).sum();
            }

            currentParts.add(para);
            currentWordCount += paraWords;
        }

        // Flush remaining
        if (!currentParts.isEmpty()) {
            String remaining = String.join("\n\n", currentParts);
            if (countWords(remaining) > 10) {
                chunks.add(createChunk(remaining, metadata, chunks.size()));
            }
        }

        log.info("Created {} chunks from text ({} chars)", chunks.size(), text.length());
        return chunks;
    }

    private List<String> splitParagraphs(String text) {
        String[] parts = text.split("\n\n");
        List<String> result = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private List<String> splitBySentences(String text) {
        String[] sentences = text.split("(?<=[.!?])\\s+");
        List<String> chunks = new ArrayList<>();
        List<String> current = new ArrayList<>();
        int currentWords = 0;

        for (String sentence : sentences) {
            int sWords = countWords(sentence);
            if (currentWords + sWords > chunkSize && !current.isEmpty()) {
                chunks.add(String.join(" ", current));
                current = new ArrayList<>();
                currentWords = 0;
            }
            current.add(sentence);
            currentWords += sWords;
        }
        if (!current.isEmpty()) {
            chunks.add(String.join(" ", current));
        }
        return chunks;
    }

    private List<String> getOverlapParts(List<String> parts) {
        List<String> overlap = new ArrayList<>();
        int overlapWords = 0;
        for (int i = parts.size() - 1; i >= 0; i--) {
            int w = countWords(parts.get(i));
            if (overlapWords + w <= chunkOverlap) {
                overlap.add(0, parts.get(i));
                overlapWords += w;
            } else {
                break;
            }
        }
        return overlap;
    }

    private TextChunk createChunk(String text, Map<String, Object> metadata, int index) {
        Map<String, Object> chunkMeta = new HashMap<>(metadata != null ? metadata : Map.of());
        chunkMeta.put("chunk_index", index);
        return new TextChunk(text, chunkMeta, countWords(text));
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }

    /**
     * Represents a single text chunk with metadata.
     */
    public record TextChunk(String text, Map<String, Object> metadata, int wordCount) {}
}
