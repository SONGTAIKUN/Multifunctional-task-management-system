package com.example.taskmanager.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.User;
import com.example.taskmanager.model.Task.Status;
import com.example.taskmanager.model.Task.Priority;
import org.springframework.http.ResponseEntity;

import java.security.Principal;
import java.util.Map;

/**
 * Service interface for administrator-level operations.
 * Provides methods to manage users, tasks, and monitor system status.
 */
public interface AdminService {

    /**
     * Retrieves a paginated list of all registered users.
     *
     * @param page The page number (0-based).
     * @param size The number of users per page.
     * @return A paginated list of User entities.
     */
    IPage<User> getUsersPage(int page, int size);

    /**
     * Updates the role of a specific user by user ID.
     *
     * @param id      The ID of the user whose role is to be updated.
     * @param newRole The new role to assign (e.g., "USER", "ADMIN").
     * @return A ResponseEntity containing the result message and status code.
     */
    ResponseEntity<String> updateUserRole(Long id, String newRole);

    /**
     * Retrieves a paginated and filtered list of all tasks in the system.
     * Supports filtering by status, priority, keyword, and username.
     *
     * @param page     The page number (0-based).
     * @param size     The number of tasks per page.
     * @param status   Optional task status filter.
     * @param priority Optional task priority filter.
     * @param keyword  Optional keyword to search in title/description.
     * @param username Optional filter to show tasks by specific user.
     * @return A paginated list of TaskDTOs.
     */
    IPage<TaskDTO> getAllTasksByAdmin(int page, int size,
                                    Status status, Priority priority,
                                    String keyword, String username);

    /**
     * Creates a new task for a specific user based on the provided data.
     *
     * @param taskData A map containing task information including target username.
     * @return A ResponseEntity with the created task DTO or error message.
     */
    ResponseEntity<?> createTaskForUser(Map<String, Object> taskData);

    /**
     * Returns basic system status information such as CPU load,
     * memory usage, disk space, and active MySQL connections.
     *
     * @return A ResponseEntity with system metrics in a map format.
     */
    ResponseEntity<?> getSystemStatus();

    /**
     * Exports all tasks in the system into an Excel file.
     * Only accessible to authenticated users (admin).
     *
     * @param principal The security context representing the logged-in user.
     * @return A byte array representing the Excel file.
     */
    byte[] exportAllTasksToExcel(Principal principal);

}
