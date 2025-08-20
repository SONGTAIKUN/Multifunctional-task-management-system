package com.example.taskmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserMapper;
import com.example.taskmanager.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")     // Base path prefix for all endpoints in this controller
public class UserApiController {

    private final JwtUtil jwtUtil;    // Utility class to handle JWT token operations

    private final UserMapper userMapper;     // Repository to access user data from the database

    public UserApiController(JwtUtil jwtUtil, UserMapper userMapper) {
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    /**
     * Endpoint to get the current authenticated user's information.
     * Requires a valid JWT token in the Authorization header.
     *
     * @param request the HTTP request object
     * @return user's information or an error message
     */
    @GetMapping("/userinfo")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {

        // Extract the Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Check if the header is missing or malformed
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or malformed Authorization header");
        }

        // Remove the "Bearer " prefix to get the actual token
        String token = authHeader.substring(7); // 去掉 "Bearer "

        // Validate the token
        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("The token is invalid or expired.");
        }

        // Extract the username from the token
        String username = jwtUtil.extractUsername(token);

        // Retrieve the user from the database
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist");
        }

        // Get the roles (authorities) from the Spring Security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> roles = authentication != null
                ? authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList())
                : List.of();

        // Build the response with user information
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("avatarUrl", user.getAvatarUrl());
        response.put("createdAt", user.getCreatedAt());
        response.put("roles", roles);

        // Return user info as a JSON response
        return ResponseEntity.ok(response);
    }
}
