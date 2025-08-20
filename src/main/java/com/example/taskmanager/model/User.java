package com.example.taskmanager.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity class representing a User in the system.
 * Maps to the "users" table in the database.
 */
@Data
@TableName("users")                  
public class User {

    // Primary key with auto-increment
    @TableId(value = "id", type = IdType.AUTO)   
    private Long id;

    // Unique username used for login
    @TableField("username")
    private String username;

    // Encrypted password for authentication
    @TableField("password")
    private String password;

    // User's email address
    @TableField("email")
    private String email;

    // User's phone number
    @TableField("phone")
    private String phone;

    // Timestamp indicating when the user account was created
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // File name or URL of the user's avatar image
    @TableField("avatar_url")
    private String avatarUrl;

    // User role, e.g., "USER" or "ADMIN"
    @TableField("role")
    private String role;
}

