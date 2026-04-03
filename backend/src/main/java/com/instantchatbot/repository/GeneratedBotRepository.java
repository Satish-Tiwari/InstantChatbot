package com.instantchatbot.repository;

import com.instantchatbot.entity.GeneratedBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access repository for GeneratedBot entities.
 * Manages the metadata for physical chatbot packages.
 */
@Repository
public interface GeneratedBotRepository extends JpaRepository<GeneratedBot, Long> {

    /**
     * Retrieves the generated bot metadata for a specific project.
     *
     * @param projectId the identifier of the project
     * @return an Optional containing the bot metadata if it has been generated
     */
    Optional<GeneratedBot> findByProjectId(Long projectId);
}
