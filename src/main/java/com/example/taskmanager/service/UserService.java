package com.example.taskmanager.service;

import com.example.taskmanager.model.User;
import com.example.taskmanager.model.UserRegisterRequest;

/**
 * Service interface for managing user-related operations such as registration and password updates.
 */
public interface UserService {

    /**
     * Registers a new user with the provided registration request data.
     *
     * @param req The user registration request containing username, password, email, etc.
     * @return The newly created User entity.
     */
    User register(UserRegisterRequest req);

    /**
     * Changes the password for a given user after validating the old password.
     *
     * @param username    The username of the user who wants to change the password.
     * @param oldPassword The current password to validate.
     * @param newPassword The new password to be set.
     * @return True if the password was successfully changed, false otherwise.
     */
    boolean changePassword(String username, String oldPassword, String newPassword);
}