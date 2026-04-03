package com.instantchatbot.service;

import com.instantchatbot.dto.request.LoginRequest;
import com.instantchatbot.dto.request.RegisterRequest;
import com.instantchatbot.dto.response.AuthResponse;
import com.instantchatbot.entity.User;
import com.instantchatbot.exception.BadRequestException;
import com.instantchatbot.repository.UserRepository;
import com.instantchatbot.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class handling all user authentication-related operations.
 * This includes user registration, login via JWT, and retrieving the current user context.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Constructs the AuthService with necessary components.
     *
     * @param userRepository the database repository for user entities
     * @param passwordEncoder the component for secure password hashing
     * @param jwtTokenProvider the component for generating and validating JWT tokens
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * Registers a new user in the system.
     *
     * @param request the registration details including name, email, and password
     * @return an AuthResponse containing the user's new JWT and account details
     * @throws BadRequestException if the email is already registered
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);
        log.info("User registered: {}", user.getEmail());

        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .userId(user.getId())
                .build();
    }

    /**
     * Authenticates an existing user and generates a JWT.
     *
     * @param request the login credentials (email and password)
     * @return an AuthResponse containing the fresh JWT and user context
     * @throws BadCredentialsException if authentication fails
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        log.info("User logged in: {}", user.getEmail());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getEmail());

        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .name(user.getName())
                .userId(user.getId())
                .build();
    }

    /**
     * Retrieves a user entity by its database identifier.
     *
     * @param userId the unique identifier of the user
     * @return the User entity
     * @throws BadRequestException if the user does not exist
     */
    public User getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }
}
