package com.example.taskmanager.controller;

import com.example.taskmanager.service.AdminService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.example.taskmanager.model.Task.Status;
import com.example.taskmanager.model.Task.Priority;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.security.Principal;


@RestController
@RequestMapping("/api/admin")     // All endpoints in this controller will be prefixed with /api/admin
public class AdminController {

    @Autowired          // Automatically inject the AdminService
    private AdminService adminService;

    /**
     * Get a paginated list of all users.
     * Accessible via GET /api/admin/users?page=0&size=10
     */
    @GetMapping("/users")
    public ResponseEntity<IPage<User>> getUsersPage(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        IPage<User> result = adminService.getUsersPage(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Update the role of a user (e.g., ROLE_USER -> ROLE_ADMIN).
     * Accessible via PUT /api/admin/users/{id}/role with JSON body: {"role": "ROLE_ADMIN"}
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newRole = body.get("role");
        return adminService.updateUserRole(id, newRole);
    }

    /**
     * Get a paginated and filtered list of all users' tasks.
     * Admin can filter by status, priority, keyword (in title/description), and username.
     * Accessible via GET /api/admin/tasks?...params...
     */
    @GetMapping("/tasks")
    public ResponseEntity<IPage<TaskDTO>> getAllTasks(@RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(required = false) Status status,
                                                      @RequestParam(required = false) Priority priority,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String username) {
        IPage<TaskDTO> result = adminService.getAllTasksByAdmin(page, size, status, priority, keyword, username);
        return ResponseEntity.ok(result);
    }

    /**
     * Admin creates a task for another user.
     * Accessible via POST /api/admin/tasks/create-for-user with task details in request body.
     * Expected JSON body includes: title, description, deadline, priority, status, and target username.
     */
    @PostMapping("/tasks/create-for-user")
    public ResponseEntity<?> createTaskForUser(@RequestBody Map<String, Object> taskData) {
        return adminService.createTaskForUser(taskData);
    }

    /**
     * Get system status info (e.g., user/task counts or other admin metrics).
     * Accessible via GET /api/admin/system/status
     */
    @GetMapping("/system/status")
    public ResponseEntity<?> getSystemStatus() {
        return adminService.getSystemStatus();
    }

    /**
     * Export all user tasks into an Excel file.
     * Accessible via GET /api/admin/tasks/export
     * Returns: application/octet-stream with attached "all_tasks.xlsx"
     */
    @GetMapping("/tasks/export")
    public ResponseEntity<byte[]> exportAllUserTasksToExcel(Principal principal) {
        // Generate the Excel file as byte array
        byte[] excelData = adminService.exportAllTasksToExcel(principal);

        // Set download headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "all_tasks.xlsx");

        // Return the Excel file
        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }
}
