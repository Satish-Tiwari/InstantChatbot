package com.instantchatbot.controller;

import com.instantchatbot.dto.request.LoginRequest;
import com.instantchatbot.dto.request.RegisterRequest;
import com.instantchatbot.dto.response.AuthResponse;
import com.instantchatbot.entity.User;
import com.instantchatbot.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for handling user authentication and registration.
 * Provides endpoints for sign-up, sign-in, and retrieving user context.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructs the AuthController with required services.
     *
     * @param authService the service handling authentication logic
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registers a new user.
     *
     * @param request user registration details
     * @return a response containing the new user's JWT and profile
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and returns a token.
     *
     * @param request login credentials
     * @return a response containing the JWT and user profile
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the profile of the currently authenticated user.
     *
     * @param user the authenticated user principal
     * @return a map containing basic user profile details
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName()
        ));
    }
}
