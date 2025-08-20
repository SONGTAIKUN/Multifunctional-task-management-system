package com.example.taskmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskMapper;   
import com.example.taskmanager.repository.UserMapper;  
import com.example.taskmanager.service.AdminService;
import com.example.taskmanager.util.TaskConverter;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.security.Principal;


/**
 * Implementation of AdminService.
 *
 * Provides operations for:
 * - User management
 * - Task management
 * - System monitoring
 * - Exporting tasks to Excel
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;   

    @Autowired
    private TaskMapper taskMapper;  

    @Autowired
    private DataSource dataSource;

    /**
     * Fetch a paginated list of all users.
     *
     * @param page current page index (0-based, converted to MyBatis-Plus 1-based)
     * @param size number of records per page
     * @return paginated user list
     */
    @Override
    public IPage<User> getUsersPage(int page, int size) {
        Page<User> mpPage = new Page<>(page + 1L, size);
        return userMapper.selectPage(mpPage, new QueryWrapper<>());
    }

    /**
     * Update a user's role by ID.
     *
     * @param id user ID
     * @param newRole new role ("USER" or "ADMIN")
     * @return response with success or error message
     */
    @Override
    public ResponseEntity<String> updateUserRole(Long id, String newRole) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return ResponseEntity.status(404).body("User does not exist");
        }
        if (!"USER".equals(newRole) && !"ADMIN".equals(newRole)) {
            return ResponseEntity.badRequest().body("Invalid role");
        }
        user.setRole(newRole);
        userMapper.updateById(user);
        return ResponseEntity.ok("Role updated successfully");
    }

    /**
     * Fetch a paginated list of tasks with optional filters:
     * - status
     * - priority
     * - keyword (search in title or description)
     * - username (fuzzy match, converted to userId list)
     *
     * @return paginated list of TaskDTOs
     */
    @Override
    public IPage<TaskDTO> getAllTasksByAdmin(int page, int size,
                                             Task.Status status,
                                             Task.Priority priority,
                                             String keyword,
                                             String username) {

        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String un = (username == null || username.isBlank()) ? null : username.trim();

        // Resolve user IDs from username filter
        List<Long> userIds = null;
        if (un != null) {
            List<User> users = userMapper.selectList(
                    new LambdaQueryWrapper<User>().like(User::getUsername, un));
            if (users.isEmpty()) {
                return new Page<>(page + 1L, size);
            }
            userIds = users.stream().map(User::getId).collect(Collectors.toList());
        }

        // Build query wrapper
        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<Task>()
                .eq(Task::getDeleted, false);

        if (status != null) {
            qw.eq(Task::getStatus, status);
        }
        if (priority != null) {
            qw.eq(Task::getPriority, priority);
        }
        if (kw != null) {
            qw.and(w -> w.like(Task::getTitle, kw)
                         .or()
                         .like(Task::getDescription, kw));
        }
        if (userIds != null) {
            qw.in(Task::getUserId, userIds);
        }

        // Order by deadline
        Page<Task> mpPage = new Page<>(page + 1L, size);
        qw.orderByAsc(Task::getDeadline);

        IPage<Task> taskPage = taskMapper.selectPage(mpPage, qw);

        Page<TaskDTO> dtoPage = new Page<>(taskPage.getCurrent(), taskPage.getSize(), taskPage.getTotal());
        dtoPage.setRecords(
                taskPage.getRecords().stream().map(t -> {
                    TaskDTO dto = TaskConverter.convertToDTO(t);
                    User u = userMapper.selectById(t.getUserId());
                    if (u != null) {
                        dto.setUsername(u.getUsername());
                    }
                    return dto;
                }).collect(Collectors.toList())
        );
        return dtoPage;
    }

    /**
     * Create a new task for a specific user by username.
     *
     * @param taskData map containing task fields (title, description, priority, status, deadline, username)
     * @return created task DTO or error response
     */
    @Override
    public ResponseEntity<?> createTaskForUser(Map<String, Object> taskData) {
        String username = (String) taskData.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body("Missing username");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username));

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("The specified user does not exist");
        }

        Task task = new Task();
        task.setUserId(user.getId());
        task.setTitle((String) taskData.get("title"));
        task.setDescription((String) taskData.get("description"));

        Object p = taskData.get("priority");
        if (p != null) {
            task.setPriority(Task.Priority.valueOf(p.toString()));
        }

        Object st = taskData.get("status");
        task.setStatus(st != null ? Task.Status.valueOf(st.toString()) : Task.Status.PENDING);

        Object ddl = taskData.get("deadline");
        if (ddl != null) {
            task.setDeadline(LocalDateTime.parse(ddl.toString()));
        }

        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDeleted(false);

        taskMapper.insert(task);

        TaskDTO dto = TaskConverter.convertToDTO(task);
        dto.setUsername(user.getUsername());
        return ResponseEntity.ok(dto);
    }

    /**
     * Retrieve system status information including:
     * - CPU load
     * - JVM memory usage
     * - System memory usage
     * - Disk space
     * - MySQL connection status
     *
     * @return system status map
     */
    @Override
    public ResponseEntity<?> getSystemStatus() {
        Map<String, Object> status = new LinkedHashMap<>();

        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        Runtime runtime = Runtime.getRuntime();

        double cpu = osBean.getCpuLoad();
        status.put("CPU Load", String.format("%.2f%%", cpu * 100));

        long jvmUsed = runtime.totalMemory() - runtime.freeMemory();
        long jvmTotal = runtime.totalMemory();
        status.put("JVM Memory Usage", String.format("%.2f MB / %.2f MB",
                jvmUsed / 1024.0 / 1024,
                jvmTotal / 1024.0 / 1024));

        long totalMem = osBean.getTotalMemorySize();
        long freeMem  = osBean.getFreeMemorySize();
        status.put("System Memory Usage", String.format("%.2f GB / %.2f GB",
                (totalMem - freeMem) / 1024.0 / 1024 / 1024,
                totalMem / 1024.0 / 1024 / 1024));

        File root = new File("/");
        status.put("Disk Space", String.format("%.2f GB available / %.2f GB total",
                root.getUsableSpace() / 1024.0 / 1024 / 1024,
                root.getTotalSpace() / 1024.0 / 1024 / 1024));

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW STATUS LIKE 'Threads_connected'")) {
            if (rs.next()) {
                status.put("MySQL Connections", rs.getString("Value"));
            }
        } catch (Exception e) {
            status.put("MySQL Connections", "Query failed: " + e.getMessage());
        }

        return ResponseEntity.ok(status);
    }


    /**
     * Export all tasks to an Excel file (.xlsx format).
     *
     * @param principal the current authenticated user (not used here)
     * @return byte array representing the Excel file
     */
    @Override
    public byte[] exportAllTasksToExcel(Principal principal) {
        List<Task> tasks = taskMapper.selectList(new QueryWrapper<>());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Tasks");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Task ID");
            header.createCell(1).setCellValue("Title");
            header.createCell(2).setCellValue("Status");
            header.createCell(3).setCellValue("Priority");
            header.createCell(4).setCellValue("Deadline");
            header.createCell(5).setCellValue("Created At");
            header.createCell(6).setCellValue("User");

            int rowNum = 1;
            for (Task task : tasks) {
                Row row = sheet.createRow(rowNum++);
                User u = (task.getUserId() != null) ? userMapper.selectById(task.getUserId()) : null;

                row.createCell(0).setCellValue(task.getId() != null ? task.getId() : -1);
                row.createCell(1).setCellValue(task.getTitle() != null ? task.getTitle() : "NULL");
                row.createCell(2).setCellValue(task.getStatus() != null ? task.getStatus().toString() : "NULL");
                row.createCell(3).setCellValue(task.getPriority() != null ? task.getPriority().toString() : "NULL");
                row.createCell(4).setCellValue(task.getDeadline() != null ? task.getDeadline().toString() : "NULL");
                row.createCell(5).setCellValue(task.getCreatedAt() != null ? task.getCreatedAt().toString() : "NULL");
                row.createCell(6).setCellValue(u != null && u.getUsername() != null ? u.getUsername() : "NULL");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Export to Excel failed", e);
        }
    }
}
