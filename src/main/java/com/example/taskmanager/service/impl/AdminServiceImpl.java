package com.example.taskmanager.service.impl;

import com.example.taskmanager.service.AdminService;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Optional;

import java.util.Map;
import java.sql.*;

import javax.sql.DataSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.taskmanager.model.Task.Status;
import com.example.taskmanager.model.Task.Priority;
import com.example.taskmanager.util.TaskConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import java.security.Principal;

/**
 * Implementation of admin-related operations, including user management,
 * task management, system monitoring, and Excel export.
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private DataSource dataSource;

    /**
     * Fetch a paginated list of all users.
     */
    @Override
    public Page<User> getUsersPage(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size));
    }

    /**
     * Update the role of a specific user by ID.
     */
    @Override
    public ResponseEntity<String> updateUserRole(Long id, String newRole) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body("User does not exist");
        }

        if (!"USER".equals(newRole) && !"ADMIN".equals(newRole)) {
            return ResponseEntity.badRequest().body("Invalid role");
        }

        User user = optionalUser.get();
        user.setRole(newRole);
        userRepository.save(user);
        return ResponseEntity.ok("Character updated successfully");
    }

    /**
     * Admin query for all tasks with optional filters (status, priority, keyword, username),
     * returning a paginated list of TaskDTOs.
     */
    @Override
    public Page<TaskDTO> getAllTasksByAdmin(int page, int size, Status status, Priority priority, String keyword, String username) {
        String keywordProcessed = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        String usernameProcessed = (username == null || username.trim().isEmpty()) ? null : username.trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by("deadline").ascending());
        Page<Task> result = taskRepository.filterAllTasksByConditions(status, priority, keywordProcessed, usernameProcessed, pageable);

        // Convert each Task entity to TaskDTO
        return result.map(task -> {
            TaskDTO dto = TaskConverter.convertToDTO(task);  
            return dto;
        });
    }

    /**
     * Create a task for a specified user by username.
     */
    @Override
    public ResponseEntity<?> createTaskForUser(Map<String, Object> taskData) {
        String username = (String) taskData.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("Missing username");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The specified user does not exist");
        }

        // Create and populate a new Task entity
        Task task = new Task();
        task.setUserId(user.getId());
        task.setTitle((String) taskData.get("title"));
        task.setDescription((String) taskData.get("description"));
        task.setPriority(Task.Priority.valueOf((String) taskData.get("priority")));

        if (taskData.containsKey("status")) {
            task.setStatus(Task.Status.valueOf((String) taskData.get("status")));
        } else {
            task.setStatus(Task.Status.PENDING);
        }

        if (taskData.containsKey("deadline") && taskData.get("deadline") != null) {
            task.setDeadline(LocalDateTime.parse((String) taskData.get("deadline")));
        }

        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDeleted(false);

        Task savedTask = taskRepository.save(task);

        TaskDTO dto = TaskConverter.convertToDTO(savedTask);
        return ResponseEntity.ok(dto);
    }

    /**
     * Returns system monitoring information including CPU load, memory usage,
     * disk space, and MySQL connection status.
     */
    @Override
    public ResponseEntity<?> getSystemStatus() {
        Map<String, Object> status = new LinkedHashMap<>();

        // JVM and system statistics
        com.sun.management.OperatingSystemMXBean osBean =
            (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        Runtime runtime = Runtime.getRuntime();

        status.put("CPU Load", String.format("%.2f%%", osBean.getSystemCpuLoad() * 100));
        status.put("JVM Memory Usage", String.format("%.2f MB / %.2f MB",
            (runtime.totalMemory() - runtime.freeMemory()) / 1024.0 / 1024,
            runtime.totalMemory() / 1024.0 / 1024));
        status.put("System Memory Usage", String.format("%.2f GB / %.2f GB",
            (osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize()) / 1024.0 / 1024 / 1024,
            osBean.getTotalPhysicalMemorySize() / 1024.0 / 1024 / 1024));

        // Disk usage
        File root = new File("/");
        status.put("Disk Space", String.format("%.2f GB 可用 / %.2f GB 总",
            root.getUsableSpace() / 1024.0 / 1024 / 1024,
            root.getTotalSpace() / 1024.0 / 1024 / 1024));

        // Check MySQL connections
        try (Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_connected'")) {

            if (rs.next()) {
                status.put("MySQL Connections", rs.getString("Value"));
            }
        } catch (Exception e) {
            status.put("MySQL Connections", "查询失败：" + e.getMessage());
        }

        return ResponseEntity.ok(status);
    }

    /**
     * Export all tasks to an Excel spreadsheet (.xlsx format).
     * Returns the file as a byte array.
     */
    @Override
    public byte[] exportAllTasksToExcel(Principal principal) {
        List<Task> tasks = taskRepository.findAll();  

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tasks");

            // Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("task ID");
            header.createCell(1).setCellValue("title");
            header.createCell(2).setCellValue("status");
            header.createCell(3).setCellValue("priority");
            header.createCell(4).setCellValue("deadline");
            header.createCell(5).setCellValue("creation time");
            header.createCell(6).setCellValue("user");

            // Populate data rows
            int rowNum = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(task.getId() != null ? task.getId() : -1);
                row.createCell(1).setCellValue(task.getTitle() != null ? task.getTitle() : "NULL");
                row.createCell(2).setCellValue(task.getStatus() != null ? task.getStatus().toString() : "NULL");
                row.createCell(3).setCellValue(task.getPriority() != null ? task.getPriority().toString() : "NULL");
                row.createCell(4).setCellValue(task.getDeadline() != null ? task.getDeadline().toString() : "NULL");
                row.createCell(5).setCellValue(task.getCreatedAt() != null ? task.getCreatedAt().toString() : "NULL");
                row.createCell(6).setCellValue(task.getUser() != null && task.getUser().getUsername() != null ? task.getUser().getUsername() : "NULL");
            }

            // Convert to byte array
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Export to Excel failed", e);
        }
    }
}
