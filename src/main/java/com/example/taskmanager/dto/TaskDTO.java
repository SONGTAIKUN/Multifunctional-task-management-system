package com.example.taskmanager.dto;

import com.example.taskmanager.model.Task.Priority;
import com.example.taskmanager.model.Task.Status;
import lombok.Data;

import java.time.LocalDateTime;

@Data   // Lombok annotation to generate getters, setters, toString, equals, and hashCode methods
public class TaskDTO {

    // Unique identifier of the task
    private Long id;

    // Title of the task
    private String title;

    // Detailed description of the task
    private String description;

    // Priority level of the task (e.g., HIGH, MEDIUM, LOW)
    private Priority priority;

    // Current status of the task (e.g., PENDING, IN_PROGRESS, COMPLETED)
    private Status status;

    // Deadline or due date of the task
    private LocalDateTime deadline;

    // Timestamp when the task was created
    private LocalDateTime createdAt;

    // Timestamp when the task was last updated
    private LocalDateTime updatedAt;

    // Indicates whether the task is soft-deleted
    private boolean isDeleted;

    // ID of the user to whom the task belongs
    private Long userId;       

    // Username of the task owner (optional field for display purposes)
    private String username;  
}
