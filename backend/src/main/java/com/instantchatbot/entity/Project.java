package com.instantchatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a chatbot project associated with a specific website URL.
 * Orchestrates the crawling, processing, and generation status of the chatbot.
 */
@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    /**
     * Unique identifier for the project.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Human-readable name of the project.
     */
    @Column(nullable = false)
    private String name;

    /**
     * The root URL of the website to be crawled and transformed into a chatbot.
     */
    @Column(nullable = false)
    private String websiteUrl;

    /**
     * Current lifecycle status of the project (e.g., PENDING, CRAWLING, READY).
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PENDING;

    /**
     * The user who owns this project.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Details of the latest or active crawl job for this project.
     */
    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private CrawlJob crawlJob;

    /**
     * Information about the generated chatbot package (ZIP).
     */
    @OneToOne(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private GeneratedBot generatedBot;

    /**
     * Total number of unique pages discovered during the crawl.
     */
    private Integer pagesFound;

    /**
     * Total number of semantic text chunks generated for the RAG pipeline.
     */
    private Integer chunksCreated;

    /**
     * Timestamp when the project was created.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp of the last update to the project metadata or status.
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
