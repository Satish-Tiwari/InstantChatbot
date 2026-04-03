package com.instantchatbot.repository;

import com.instantchatbot.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access repository for User entities.
 * Supports operations for user identity management and authentication.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email to search for
     * @return an Optional containing the found User, or empty if none matches
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user already exists with the given email.
     *
     * @param email the email to check
     * @return true if a user with this email exists
     */
    boolean existsByEmail(String email);
}
