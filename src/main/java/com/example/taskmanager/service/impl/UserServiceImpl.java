package com.example.taskmanager.service.impl;

import com.example.taskmanager.model.User;
import com.example.taskmanager.model.UserRegisterRequest;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementation of the UserService interface.
 * Handles user registration and password management.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Constructor-based dependency injection for UserRepository.
     * Initializes BCryptPasswordEncoder for secure password hashing.
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); 
    }

    /**
     * Registers a new user by saving user details to the database.
     * Password is hashed using BCrypt before saving.
     *
     * @param req The user registration request containing username, password, email, and phone.
     * @return The saved User object.
     */
    @Override
    public User register(UserRegisterRequest req) {
        User user = new User();
        user.setUsername(req.getUsername());

        // Hash the password before storing
        String hashedPassword = passwordEncoder.encode(req.getPassword());
        user.setPassword(hashedPassword);

        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());

        // Save the new user to the database
        return userRepository.save(user);
    }

    /**
     * Changes the password for the given username.
     * Verifies the old password before applying the new one.
     *
     * @param username     The user's username.
     * @param oldPassword  The current password (plain text).
     * @param newPassword  The new password to set (plain text).
     * @return true if the password is successfully changed, false otherwise.
     */
    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username);
        if (user == null) return false;

        // Check if the old password matches the stored hashed password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;  
        }

        // Encode and update the new password
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedNewPassword);
        userRepository.save(user);
        return true;
    }
}
