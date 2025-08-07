package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Task.Priority;
import com.example.taskmanager.model.Task.Status;
import com.example.taskmanager.service.TaskService;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.taskmanager.model.Task;
import java.security.Principal;

@RestController
@RequestMapping("/api/tasks")      // Base URL path for all task-related APIs
public class TaskController {

    @Autowired
    private TaskService taskService;       // Injects the service responsible for business logic

    /**
     * Create a new task for the currently logged-in user.
     *
     * @param task the task entity received from the client
     * @param principal provides access to the authenticated user's details
     * @return created task as a DTO
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@RequestBody Task task, Principal principal) {
        return ResponseEntity.ok(taskService.createTask(task, principal));
    }

    /**
     * Get all tasks created by the currently logged-in user (paginated).
     *
     * @param page the page number (default 0)
     * @param size the number of items per page (default 10)
     * @param principal the authenticated user
     * @return a page of TaskDTOs
     */
    @GetMapping("/mine")
    public ResponseEntity<?> getAllTasksForUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        Page<TaskDTO> dtoPage = taskService.getAllTasksForUser(principal, page, size);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Filter tasks created by the current user based on optional criteria.
     *
     * @param status optional task status filter
     * @param priority optional task priority filter
     * @param keyword optional keyword for searching title/description
     * @param page the page number (default 0)
     * @param size the number of items per page (default 10)
     * @param principal the authenticated user
     * @return a filtered page of TaskDTOs
     */
    @GetMapping("/mine/filter")
    public ResponseEntity<Page<TaskDTO>> filterTasks(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal) {

        Page<TaskDTO> dtoPage = taskService.filterTasks(principal, status, priority, keyword, page, size);
        return ResponseEntity.ok(dtoPage);
    }

    /**
     * Retrieve task details by ID. Only accessible by task owner or admin.
     *
     * @param id the ID of the task
     * @param principal the authenticated user
     * @return TaskDTO if found, or an error message
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> getTaskById(@PathVariable Long id, Principal principal) {
        try {
            TaskDTO dto = taskService.getTaskById(id, principal);
            return ResponseEntity.ok(dto);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No permission to view this task");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task does not exist");
        }
    }

    /**
     * Update a task by ID. Only accessible by task owner or admin.
     *
     * @param id the ID of the task
     * @param updatedTask task data to update
     * @param principal the authenticated user
     * @return updated task as a DTO, or error message
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> updateTask(@PathVariable Long id,
                                        @RequestBody Task updatedTask,
                                        Principal principal) {
        try {
            TaskDTO dto = taskService.updateTask(id, updatedTask, principal);
            return ResponseEntity.ok(dto);
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No permission to modify this task");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task does not exist");
        }
    }

    /**
     * Soft delete a task by ID. Only accessible by task owner or admin.
     *
     * @param id the ID of the task
     * @param principal the authenticated user
     * @return success message or error message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Principal principal) {
        try {
            taskService.deleteTask(id, principal);
            return ResponseEntity.ok("Task soft deleted");
        } catch (IllegalAccessException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No permission to delete this task");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Task does not exist");
        }
    }
}
