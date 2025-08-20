package com.example.taskmanager.controller; 


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.security.Principal;

@RestController
@RequestMapping("/api")   // All endpoints in this controller will be prefixed with "/api"
public class AvatarUploadController {

    @Value("${avatar.upload-dir}")      // Injects the configured avatar storage directory from application.properties/yaml
    private String avatarDir;

    private final UserMapper userMapper;     // Injects UserMapper
    
    public AvatarUploadController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * Endpoint to upload a user's avatar image.
     * The image must be in JPG format.
     * Any existing avatar will be deleted and replaced with the new one.
     *
     * @param file the uploaded avatar file
     * @param principal the authenticated user info (automatically resolved from JWT)
     * @return ResponseEntity indicating success or failure
     */
    @PostMapping("/upload-avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar_img") MultipartFile file,
                                          Principal principal) {

        // Get the username of the currently authenticated user
        String username = principal.getName();  
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username)
        );

        // If the user does not exist, return 401 Unauthorized
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User does not exist");
        }

        // Check if the uploaded file is of allowed MIME type
        String contentType = file.getContentType();
        if (!contentType.equalsIgnoreCase("image/jpeg") && !contentType.equalsIgnoreCase("image/jpg")) {
            return ResponseEntity.badRequest().body("Only JPG images are supported for uploading");
        }

        try {
            // If the user has previously uploaded an avatar, delete the old one
            String oldFileName = user.getAvatarUrl();
            if (oldFileName != null && !oldFileName.isEmpty()) {
                File oldFile = new File(avatarDir, oldFileName);
                if (oldFile.exists() && oldFile.isFile()) {
                    oldFile.delete();   // Delete old avatar file
                }
            }

            // Generate a new unique filename for the uploaded avatar
            String fileName = UUID.randomUUID() + ".jpg";
            File savePath = new File(avatarDir, fileName);

            // Save the uploaded file to disk
            file.transferTo(savePath);

            // Update the user's avatar URL in the database
            user.setAvatarUrl(fileName);
            userMapper.updateById(user);

            // Return success response
            return ResponseEntity.ok("Upload successful");
        } catch (IOException e) {
            // If file saving fails, return 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File save failed");
        }
    }
}
