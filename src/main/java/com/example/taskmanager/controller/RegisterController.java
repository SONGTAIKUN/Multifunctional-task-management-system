package com.example.taskmanager.controller;

import com.example.taskmanager.model.User;
import com.example.taskmanager.model.UserRegisterRequest;
import com.example.taskmanager.repository.UserRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime; 
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")     // Base path for all endpoints in this controller
public class RegisterController {

    @Autowired
    private UserRepository userRepository;      // Used to interact with the users table in the database

    @Autowired
    private PasswordEncoder passwordEncoder;        // For securely hashing passwords before saving

    /**
     * POST endpoint to register a new user.
     * Validates the input, checks for duplicate username/email, saves the new user if valid.
     *
     * @param req the user registration request payload (validated)
     * @param bindingResult holds validation errors, if any
     * @return HTTP response with registration result
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @Valid @RequestBody UserRegisterRequest req,    // Automatically bind and validate JSON request body
            BindingResult bindingResult     // Captures validation errors
    ) {
        Map<String, Object> response = new HashMap<>();     // Response map to return messages

        // If validation failed, return the first error message
        if (bindingResult.hasErrors()) {
            response.put("message", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return ResponseEntity.badRequest().body(response);   // 400 Bad Request
        }

        // Check if username already exists
        if (userRepository.findByUsername(req.getUsername()) != null) {
            response.put("message", "Username already exists");
            return ResponseEntity.status(409).body(response);   // 409 Conflict
        }

        // Check if email already exists
        if (userRepository.findByEmail(req.getEmail()) != null) {
            response.put("message", "Email already exists");
            return ResponseEntity.status(409).body(response);      // 409 Conflict
        }

        // If all validations pass, create a new User object
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));    // Encrypt the password
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setCreatedAt(LocalDateTime.now());     // Set account creation time
        user.setRole("USER");       // Assign default role

        // Save the user to the database
        userRepository.save(user);

        // Return success message
        response.put("message", "Successful registration");
        return ResponseEntity.ok(response);     // 200 OK
    }
}
