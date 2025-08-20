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

    /**
     * Username field:
     * - Cannot be blank
     * - Must be between 3 and 20 characters
     */
    @NotBlank(message = "Username cannot be empty")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters long")
    private String username;

    /**
     * Password field:
     * - Cannot be blank
     * - Must contain at least 8 characters
     * - Must include at least one uppercase letter, one lowercase letter, and one digit
     */
    @NotBlank(message = "Password cannot be empty")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "The password must be at least 8 characters long and contain uppercase and lowercase letters and numbers."
    )
    private String password;

    /**
     * Email field:
     * - Cannot be blank
     * - Must follow a valid email format
     */
    @NotBlank(message = "Email address cannot be empty")
    @Email(message = "The email format is incorrect")
    private String email;

    /**
     * Phone number field:
     * - Cannot be blank
     * - Can be extended later to include format validation (regex)
     */
    @NotBlank(message = "Mobile phone number cannot be empty")
    private String phone;

    /**
     * Avatar URL:
     * - Optional field
     * - Can store either an image URL or a file name reference
     */
    private String avatarUrl;

    /**
     * Default constructor (required by frameworks such as Spring).
     */
    public UserRegisterRequest() {}

    /**
     * All-argument constructor for convenience when creating objects manually.
     */
    public UserRegisterRequest(String username, String password, String email, String phone, String avatarUrl) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.avatarUrl = avatarUrl;  
    }

    // ---------- Getters and Setters ----------

    /** Get the username. */
    public String getUsername() {
        return username;
    }

    /** Set the username. */
    public void setUsername(String username) {
        this.username = username;
    }

    /** Get the password. */
    public String getPassword() {
        return password;
    }

    /** Set the password. */
    public void setPassword(String password) {
        this.password = password;
    }

    /** Get the email address. */
    public String getEmail() {
        return email;
    }

    /** Set the email address. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Get the phone number. */
    public String getPhone() {
        return phone;
    }

    /** Set the phone number. */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** Get the avatar URL. */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /** Set the avatar URL. */
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

}
