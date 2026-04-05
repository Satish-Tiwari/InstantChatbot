package com.instantchatbot.repository;

import com.instantchatbot.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access repository for Project entities.
 * Handles persistence for chatbot configuration and metadata.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Retrieves all projects for a specific user, ordered by creation date descending.
     *
     * @param userId the owner's identifier
     * @return a list of projects belonging to the user
     */
    List<Project> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Retrieves a project by ID and validates its ownership.
     *
     * @param id the project identifier
     * @param userId the expected owner's identifier
     * @return an Optional containing the project if found and owned by the user
     */
    Optional<Project> findByIdAndUserId(Long id, Long userId);

    /**
     * Checks if a project exists and belongs to a specific user.
     *
     * @param id the project identifier
     * @param userId the expected owner's identifier
     * @return true if the project exists for the given user, false otherwise
     */
    boolean existsByIdAndUserId(Long id, Long userId);
}
