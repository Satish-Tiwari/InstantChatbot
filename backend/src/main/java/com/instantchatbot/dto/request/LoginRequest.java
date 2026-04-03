package com.instantchatbot.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Data transfer object for user authentication (login).
 */
@Data
public class LoginRequest {

    /**
     * The unique email address associated with the user account.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    /**
     * Clear-text password for authentication (hashed by the service layer).
     */
    @NotBlank(message = "Password is required")
    private String password;
}
