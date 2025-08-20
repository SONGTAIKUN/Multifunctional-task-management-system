package com.example.taskmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserMapper;
import com.example.taskmanager.service.AuthService;
import com.example.taskmanager.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController     // Marks this class as a REST controller where each method returns JSON data
public class LoginController {

    @Autowired
    private AuthService authService;    // Handles authentication logic (e.g., credential verification, login throttling)

    @Autowired
    private JwtUtil jwtUtil;    // Utility for generating and validating JWT tokens

    @Autowired
    private UserMapper userMapper;      // Repository for accessing user data from the database


    /**
     * POST endpoint for user login.
     * Validates username and password, then issues a JWT token if authentication is successful.
     *
     * @param username the user's login username (from form data)
     * @param password the user's login password (from form data)
     * @param request the HTTP request used to extract client IP address
     * @return ResponseEntity with login result and JWT token if successful
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                @RequestParam String password,
                                HttpServletRequest request) {

        // Extract the client's IP address (useful for rate limiting or logging)
        String ip = request.getRemoteAddr();

        // Call AuthService to verify credentials and handle login logic
        String result = authService.login(ip, username, password);

        // Prepare the response payload
        Map<String, Object> response = new HashMap<>();

        // If login was successful
        if ("Login successful".equals(result)) {

            // Fetch user information from the database
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>().eq(User::getUsername, username)
            );

            // If user does not exist (should not happen under normal circumstances)
            if (user == null) {
                response.put("message", "User does not exist");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Prepare additional claims for the JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", user.getRole());     // Include user role in the token claims

            // Generate a JWT token with username and claims
            String token = jwtUtil.generateToken(claims, username);

            // Populate the response with token and user details
            response.put("message", result);
            response.put("username", username);
            response.put("token", token);
            response.put("role", user.getRole());

            return ResponseEntity.ok(response);      // Return 200 OK with token and role
        } else {
            // Login failed: return error message
            response.put("message", result);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
