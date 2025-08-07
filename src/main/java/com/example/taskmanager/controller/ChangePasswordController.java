package com.example.taskmanager.controller;

import com.example.taskmanager.model.ChangePasswordRequest;
import com.example.taskmanager.service.UserService;
import com.example.taskmanager.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")     // All routes in this controller will be prefixed with "/api"
public class ChangePasswordController {

    @Autowired
    private JwtUtil jwtUtil;    // Utility class for extracting user information from JWT tokens

    @Autowired
    private UserService userService;    // Service layer to handle user-related business logic

    /**
     * Endpoint to change a user's password.
     * Requires JWT authentication and a valid old password.
     *
     * @param request contains old and new passwords
     * @param httpRequest used to extract the JWT token from the Authorization header
     * @return ResponseEntity indicating success or failure
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            HttpServletRequest httpRequest) {

        // Retrieve the Authorization header from the HTTP request
        String authHeader = httpRequest.getHeader("Authorization");

        // Check if the header is present and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Missing or invalid Authorization header");
        }

        // Extract the JWT token by removing the "Bearer " prefix
        String token = authHeader.substring(7);

        // Extract the username from the token
        String username = jwtUtil.extractUsername(token);
        if (username == null) {
            return ResponseEntity.status(401).body("Invalid token");
        }

        // Extract the username from the token
        boolean success = userService.changePassword(username, request.getOldPassword(), request.getNewPassword());

        // Return appropriate response based on whether the password was successfully changed
        if (success) {
            return ResponseEntity.ok().body("{\"message\":\"Password changed successfully\"}");
        } else {
            return ResponseEntity.status(400).body("{\"message\":\"The old password is incorrect\"}");
        }
    }
}
