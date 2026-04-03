package com.instantchatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a registered user in the system.
 * Handles authentication credentials and maintains a relationship with the user's projects.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User's email address, used as a unique username for login.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * BCrypt hashed password.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Full name of the user.
     */
    @Column(nullable = false)
    private String name;

    /**
     * List of projects created by this user.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Project> projects = new ArrayList<>();

    /**
     * Timestamp when the user account was created.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
