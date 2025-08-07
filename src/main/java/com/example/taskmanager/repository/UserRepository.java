package com.example.taskmanager.repository;

import com.example.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing database operations on User entities.
 * Extends JpaRepository to provide standard CRUD functionality.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Checks if a user with the given username already exists.
     *
     * @param username the username to check
     * @return true if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user with the given email address already exists.
     *
     * @param email the email address to check
     * @return true if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Finds a user by their username.
     *
     * @param username the username to search
     * @return the User object if found, or null if not found
     */
    User findByUsername(String username);

    /**
     * Finds a user by their email address.
     *
     * @param email the email to search
     * @return the User object if found, or null if not found
     */
    User findByEmail(String email);

}