package com.example.taskmanager.service.impl;

import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Implementation of the authentication service.
 * Handles login attempts, tracks failures per IP using Redis,
 * and temporarily locks IPs that exceed the maximum allowed failures.
 */
@Service
public class AuthServiceImpl implements AuthService {

    // Redis template for interacting with the cache
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Repository to query user data from the database
    @Autowired
    private UserRepository userRepository;

    // Password encoder to securely compare raw and hashed passwords
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Maximum allowed failed login attempts before locking the IP
    private static final int MAX_FAILS = 5;

    // Duration for which the IP will be locked after too many failures
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    /**
     * Handles login logic:
     * - Tracks failed attempts per IP
     * - Locks IP if too many failed attempts occur
     * - Clears failure data upon successful login
     *
     * @param ip       IP address of the client attempting login
     * @param username Username provided
     * @param password Password provided
     * @return Login result message
     */
    @Override
    public String login(String ip, String username, String password) {
        String lockKey = "login:lock:" + ip;    // Redis key for lock status
        String failKey = "login:fail:" + ip;    // Redis key for failed attempt count

        // Check if this IP is currently locked
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            return "This IP has failed to log in too many times. Please try again in 15 minutes.";
        }

        // Fetch user from database
        User user = userRepository.findByUsername(username);

        // If user doesn't exist or password doesn't match
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            // Increment failed login count
            Long fails = redisTemplate.opsForValue().increment(failKey);
            // Set expiration on the failure key (sliding expiration)
            redisTemplate.expire(failKey, LOCK_DURATION);

            // If failures exceed threshold, lock the IP
            if (fails != null && fails >= MAX_FAILS) {
                redisTemplate.opsForValue().set(lockKey, "locked", LOCK_DURATION);
                return "Too many failed login attempts, IP locked for 15 minutes";
            }

            return "Username or password is incorrect and failed " + fails + " times.";
        }

        // Successful login: clear any existing failure records
        redisTemplate.delete(failKey);
        redisTemplate.delete(lockKey);
        return "Login successful";
    }
}
