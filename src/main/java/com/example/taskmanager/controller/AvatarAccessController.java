package com.example.taskmanager.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api")     // All routes in this controller will start with /api
public class AvatarAccessController {

    @Value("${avatar.upload-dir}")      // Inject the configured avatar storage directory from application properties
    private String avatarDir;

    /**
     * Handles HTTP GET requests to fetch an avatar image by filename.
     * Example request: GET /api/avatar/user123.jpg
     * 
     * @param filename the name of the avatar file to retrieve
     * @param authHeader optional Authorization header (not required for access)
     * @return the image as a downloadable or displayable resource
     */
    @GetMapping("/avatar/{filename:.+}")       // Matches any filename (including extensions like .jpg, .png)
    public ResponseEntity<Resource> getAvatar(
            @PathVariable String filename,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {

        // Construct the absolute path to the requested file
        Path filePath = Paths.get(avatarDir).resolve(filename).normalize();
        File file = filePath.toFile();

        // If the file does not exist or is not a regular file, return 404 Not Found
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Load the file as a Spring Resource for download or direct browser display
            Resource resource = new UrlResource(file.toURI());

            // Determine the file's MIME type (e.g., image/jpeg)
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;   // Default binary stream
            }

            // Return the file with appropriate Content-Type header
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(resource);
        } catch (MalformedURLException e) {

            // Handle invalid file paths (e.g., URL construction issues)
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();

        } catch (Exception e) {

            // Handle any other unexpected exceptions
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
            
        }
    }
}
