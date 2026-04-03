package com.instantchatbot.repository;

import com.instantchatbot.entity.CrawlJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data access repository for CrawlJob entities.
 * Tracks the background processing status for crawler tasks.
 */
@Repository
public interface CrawlJobRepository extends JpaRepository<CrawlJob, Long> {

    /**
     * Retrieves a crawl job associated with a specific project.
     *
     * @param projectId the identifier of the related project
     * @return an Optional containing the job if it exists
     */
    Optional<CrawlJob> findByProjectId(Long projectId);
}
