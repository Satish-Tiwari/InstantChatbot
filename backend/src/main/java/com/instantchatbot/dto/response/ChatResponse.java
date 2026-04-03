package com.instantchatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response structure for an AI-generated answer.
 * Includes the raw answer text, source URI citations, and a calculated confidence score.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    /** The generated answer text */
    private String answer;
    /** List of URLs used as context for this answer */
    private List<String> sources;
    /** Confidence score representing the relevance of the retrieved context */
    private double confidence;
}
