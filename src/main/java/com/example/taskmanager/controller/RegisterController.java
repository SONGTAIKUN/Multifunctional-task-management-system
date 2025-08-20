package com.example.taskmanager.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.UserRegisterRequest;
import com.example.taskmanager.repository.UserMapper;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")     // Base path for all endpoints in this controller
public class RegisterController {


    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;      // For securely hashing passwords before saving

    public RegisterController(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

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
        Map<String, Object> resp = new HashMap<>();     // Response map to return messages

        // If validation failed, return the first error message
        if (bindingResult.hasErrors()) {
            resp.put("message", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return ResponseEntity.badRequest().body(resp);
        }

        // Check if username already exists
        Long usernameCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, req.getUsername())
        );
        if (usernameCount != null && usernameCount > 0) {
            resp.put("message", "Username already exists");
            return ResponseEntity.status(409).body(resp);
        }

        // Check if email already exists
        Long emailCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())
        );
        if (emailCount != null && emailCount > 0) {
            resp.put("message", "Email already exists");
            return ResponseEntity.status(409).body(resp);
        }

        // If all validations pass, create a new User object
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));    // Encrypt the password
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole("USER");       // Assign default role

        // Save the user to the database
        userMapper.insert(user);

        // Return success message
        resp.put("message", "Successful registration");
        return ResponseEntity.ok(resp);     // 200 OK
    }
}
