package com.example.taskmanager.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity class representing a Task in the task management system.
 * Maps to the "tasks" table in the database.
 */
@Entity
@Table(name = "tasks")
@Data
public class Task {

    // Primary key with auto-increment
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign key referencing the user who owns this task
    @Column(name = "user_id")  
    private Long userId;

    // Many-to-one relationship to the User entity (lazy loading)
    // This field is read-only and mapped using user_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)  
    private User user;

    // Title of the task
    private String title;

    // Description of the task, stored as TEXT in the database
    @Column(columnDefinition = "TEXT")
    private String description;

    // Enum representing the priority level of the task
    @Enumerated(EnumType.STRING)
    private Priority priority;

    // Deadline or due date for the task
    private LocalDateTime deadline;

    // Enum representing the current status of the task
    @Enumerated(EnumType.STRING)
    private Status status;

    // Timestamp indicating when the task was created
    private LocalDateTime createdAt;

    // Timestamp indicating the last time the task was updated
    private LocalDateTime updatedAt;

    // Flag indicating whether the task is soft-deleted
    private boolean isDeleted;

    /**
     * Enum representing task priority levels.
     */
    public enum Priority { HIGH, MEDIUM, LOW }

    /**
     * Enum representing task status values.
     */
    public enum Status { COMPLETED, IN_PROGRESS, PENDING }
}
