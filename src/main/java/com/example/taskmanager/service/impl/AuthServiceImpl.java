package com.example.taskmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserMapper;   
import com.example.taskmanager.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Implementation of the authentication service.
 *
 * Provides login functionality with brute-force protection:
 * - Tracks failed login attempts per IP using Redis
 * - Locks IPs for a defined duration after exceeding max allowed failures
 * - Resets failure counters upon successful login
 */
@Service
public class AuthServiceImpl implements AuthService {

    /** Redis template for interacting with Redis cache. */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /** Mapper for querying user information from the database. */
    @Autowired
    private UserMapper userMapper;

    /** Password encoder for securely comparing raw and hashed passwords. */
    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Maximum allowed failed login attempts before locking the IP. */
    private static final int MAX_FAILS = 5;

    /** Lock duration for IPs that exceed maximum failed attempts. */
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    /**
     * Handles login process with brute-force protection.
     *
     * Logic:
     * 1. Checks if IP is locked; if yes, denies login immediately.
     * 2. Validates username and password against stored credentials.
     * 3. On failure:
     *    - Increments failure count in Redis
     *    - Locks IP if failure count exceeds threshold
     * 4. On success:
     *    - Clears any failure and lock records for the IP
     *
     * @param ip       the client IP address
     * @param username the username entered
     * @param password the password entered
     * @return message describing the result of the login attempt
     */
    @Override
    public String login(String ip, String username, String password) {
        final String lockKey = "login:lock:" + ip;
        final String failKey = "login:fail:" + ip;

        // Check if this IP is currently locked
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            return "This IP has failed to log in too many times. Please try again in 15 minutes.";
        }

        // Fetch user by username
        User user = userMapper.selectOne(
                new QueryWrapper<User>().lambda().eq(User::getUsername, username));

        // Validate credentials
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            Long fails = redisTemplate.opsForValue().increment(failKey); // 自增1
            redisTemplate.expire(failKey, LOCK_DURATION);

            if (fails != null && fails >= MAX_FAILS) {
                // Exceeded threshold -> lock IP
                redisTemplate.opsForValue().set(lockKey, "locked", LOCK_DURATION);
                return "Too many failed login attempts, IP locked for 15 minutes";
            }

            long used = (fails == null ? 1 : fails);
            long remaining = Math.max(0, MAX_FAILS - used);
            return "Username or password is incorrect. Failed " + used +
                   " time(s). Remaining before lock: " + remaining + ".";
        }

        // Successful login -> clear failure and lock records
        redisTemplate.delete(failKey);
        redisTemplate.delete(lockKey);
        return "Login successful";
    }
}
