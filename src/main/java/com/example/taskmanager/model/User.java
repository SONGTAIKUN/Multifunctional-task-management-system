package com.example.taskmanager.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Entity class representing a User in the system.
 * Maps to the "users" table in the database.
 */
@Entity  
@Table(name = "users")     
@Data
public class User {

    // Primary key with auto-increment
    @Id    
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Long id;

    // Unique username used for login
    private String username;

    // Encrypted password for authentication
    private String password;

    // User's email address
    private String email;

    // User's phone number
    private String phone;

    // Timestamp indicating when the user account was created
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // File name or URL of the user's avatar image
    @Column(name = "avatar_url")
    private String avatarUrl;

    // User role, e.g., "USER" or "ADMIN"
    private String role;
}

