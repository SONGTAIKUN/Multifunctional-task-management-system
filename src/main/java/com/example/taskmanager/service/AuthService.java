package com.example.taskmanager.service;

/**
 * Service interface for handling user authentication logic.
 * Includes methods related to login and security measures.
 */
public interface AuthService {

    /**
     * Authenticates a user with the given credentials and tracks login failures by IP.
     * Locks the IP for a certain duration after too many failed attempts.
     *
     * @param ip       The IP address of the login attempt.
     * @param username The username provided by the user.
     * @param password The password provided by the user.
     * @return A message indicating success, failure reason, or lock status.
     */
    String login(String ip, String username, String password);
}
