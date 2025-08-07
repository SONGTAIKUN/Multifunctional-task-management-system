package com.example.taskmanager.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


/**
 * DTO (Data Transfer Object) for user registration requests.
 * Includes validation annotations to ensure input integrity.
 */
public class UserRegisterRequest {

    // Username must not be blank and must be between 3 and 20 characters
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters long")
    private String username;

    // Password must not be blank and must contain at least:
    // - 8 characters
    // - one uppercase letter
    // - one lowercase letter
    // - one digit
    @NotBlank(message = "Password cannot be empty")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "The password must be at least 8 characters long and contain uppercase and lowercase letters and numbers."
    )
    private String password;

    // Email must not be blank and must be in valid email format
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "The email format is incorrect")
    private String email;

    // Phone number must not be blank
    @NotBlank(message = "Mobile phone number cannot be empty")
    private String phone;

    // Optional avatar URL or file name
    private String avatarUrl;

    // Default constructor (required for frameworks like Spring)
    public UserRegisterRequest() {}

    // All-argument constructor for convenience
    public UserRegisterRequest(String username, String password, String email, String phone, String avatarUrl) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;  
    }

    // Getters and setters for each field
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

}
