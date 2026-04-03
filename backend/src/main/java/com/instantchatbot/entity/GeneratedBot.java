package com.instantchatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Represents the final generated chatbot package for a project.
 * Stores metadata about the physical ZIP file ready for download.
 */
@Entity
@Table(name = "generated_bots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedBot {

    /**
     * Unique identifier for the generated bot record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent project this bot was generated for.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * Absolute or relative path to the generated ZIP file on the server.
     */
    @Column(nullable = false)
    private String zipFilePath;

    /**
     * Size of the generated ZIP file in bytes.
     */
    private Long fileSizeBytes;

    /**
     * Timestamp when the chatbot package was generated.
     */
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
