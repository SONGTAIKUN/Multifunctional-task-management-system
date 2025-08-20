package com.example.taskmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.UserRegisterRequest;
import com.example.taskmanager.repository.UserMapper;
import com.example.taskmanager.service.UserService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of the UserService interface.
 * 
 * Provides user registration and password management functionality.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor-based dependency injection for UserMapper and PasswordEncoder.
     */
    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user in the system.
     * 
     * Validation steps:
     * - Check for existing username
     * - Check for existing email
     * 
     * Security:
     * - The password is hashed using BCrypt before being persisted.
     *
     * @param req The user registration request containing username, password, email, phone, etc.
     * @return The saved User entity.
     * @throws IllegalArgumentException if username or email already exists
     */
    @Override
    public User register(UserRegisterRequest req) {
        // Check for existing username
        User existedByUsername = userMapper.selectOne(
                new QueryWrapper<User>().lambda().eq(User::getUsername, req.getUsername()));
        if (existedByUsername != null) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check for existing email
        User existedByEmail = userMapper.selectOne(
                new QueryWrapper<User>().lambda().eq(User::getEmail, req.getEmail()));
        if (existedByEmail != null) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Create and populate new user entity
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole("USER");                // 默认角色
        user.setCreatedAt(LocalDateTime.now());

        // Persist new user
        userMapper.insert(user);
        return user;
    }

    /**
     * Changes the password for a given user.
     * 
     * Workflow:
     * - Verify that the user exists
     * - Validate the provided old password against the stored hash
     * - Update with a newly hashed password
     *
     * @param username    the username of the account
     * @param oldPassword the current password (plain text)
     * @param newPassword the new password (plain text)
     * @return true if password was updated successfully, false otherwise
     */
    @Override
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = userMapper.selectOne(
                new QueryWrapper<User>().lambda().eq(User::getUsername, username));
        if (user == null) return false;

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        return true;
    }
}
