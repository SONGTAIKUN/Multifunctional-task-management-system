package com.example.taskmanager.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity class representing a Task in the task management system.
 * Maps to the "tasks" table in the database.
 */
@Data
@TableName("tasks")
public class Task {

    // Primary key with auto-increment
    @TableId(value = "id", type = IdType.AUTO)   
    private Long id;

    // Foreign key referencing the user who owns this task
    @TableField("user_id")
    private Long userId;

    // Many-to-one relationship to the User entity (lazy loading)
    // This field is read-only and mapped using user_id
    @TableField(exist = false)
    private User user;

    // Title of the task
    @TableField("title")
    private String title;

    // Description of the task, stored as TEXT in the database
    @TableField("description")
    private String description;  

    // Enum representing the priority level of the task
    @TableField("priority")
    private Priority priority;  

    // Deadline or due date for the task
    @TableField("deadline")
    private LocalDateTime deadline;

    // Enum representing the current status of the task
    @TableField("status")
    private Status status;

    // Timestamp indicating when the task was created
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // Timestamp indicating the last time the task was updated
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // Flag indicating whether the task is soft-deleted
    @TableField("is_deleted")
    private Boolean deleted;

    /**
     * Enum representing task priority levels.
     */
    public enum Priority { HIGH, MEDIUM, LOW }

    /**
     * Enum representing task status values.
     */
    public enum Status { COMPLETED, IN_PROGRESS, PENDING }
}
