package com.instantchatbot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response containing identity details and a specialized JWT upon successful authentication.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    /** The signed JWT for subsequent authorized requests */
    private String token;
    /** User's email */
    private String email;
    /** User's name */
    private String name;
    /** User's primary identifier */
    private Long userId;
}
