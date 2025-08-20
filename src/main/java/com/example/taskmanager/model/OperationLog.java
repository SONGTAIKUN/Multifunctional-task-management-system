package com.example.taskmanager.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;


/**
 * Entity class representing an operation log record in the system.
 * Used to audit actions performed by users on different system resources.
 */
@Data
@TableName("operation_logs")
public class OperationLog {

    // Primary key with auto-increment strategy
    @TableId(value = "id", type = IdType.AUTO)   
    private Long id;

    // The username of the user who performed the operation
    @TableField("username")
    private String username;      

    // The type of action performed (e.g., CREATE, UPDATE, DELETE)
    @TableField("action_type")
    private String actionType;       

    // The type of target being affected (e.g., "Task", "User")
    @TableField("target_type")
    private String targetType;    

    // The ID of the target resource that was affected
    @TableField("target_id")
    private Long targetId;          

    // A detailed description of the operation
    @TableField("description")
    private String description;   

    // Timestamp indicating when the operation was logged
    @TableField(value = "timestamp", fill = FieldFill.INSERT)
    private LocalDateTime timestamp;

}
