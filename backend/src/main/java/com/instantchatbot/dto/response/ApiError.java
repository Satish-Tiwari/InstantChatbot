package com.instantchatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Unified error response structure for API exceptions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {
    /** HTTP status code */
    private int status;
    /** Human-readable error message */
    private String message;
    /** Server-side timestamp of the error */
    private LocalDateTime timestamp;
}
