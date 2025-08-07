package com.example.taskmanager.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entity class representing an operation log record in the system.
 * Used to audit actions performed by users on different system resources.
 */
@Entity
@Table(name = "operation_logs")
@Data
public class OperationLog {

    // Primary key with auto-increment strategy
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The username of the user who performed the operation
    private String username;        

    // The type of action performed (e.g., CREATE, UPDATE, DELETE)
    @Column(name = "action_type")
    private String actionType;        

    // The type of target being affected (e.g., "Task", "User")
    @Column(name = "target_type")
    private String targetType;       

    // The ID of the target resource that was affected
    @Column(name = "target_id")
    private Long targetId;            

    // A detailed description of the operation
    private String description;     

    // Timestamp indicating when the operation was logged
    private LocalDateTime timestamp;  

    /**
     * Automatically sets the timestamp before the entity is persisted.
     */
    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now(); 
    }
}
