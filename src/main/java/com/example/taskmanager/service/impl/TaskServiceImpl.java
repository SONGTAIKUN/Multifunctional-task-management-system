package com.example.taskmanager.service.impl;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Task.Priority;
import com.example.taskmanager.model.Task.Status;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskRepository;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.service.TaskService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import com.example.taskmanager.util.TaskConverter;

/**
 * Implementation of TaskService.
 * Handles business logic for task creation, retrieval, filtering, updating, and deletion.
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Create a new task for the currently authenticated user.
     */
    @Override
    public TaskDTO createTask(Task task, Principal principal) {
        // Fetch the currently logged-in user
        User user = userRepository.findByUsername(principal.getName());
        if (user == null) {
            throw new RuntimeException("User does not exist");
        }

        // Set ownership and timestamps
        task.setUserId(user.getId());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDeleted(false);

        // Save task and convert to DTO
        Task savedTask = taskRepository.save(task);
        TaskDTO dto = TaskConverter.convertToDTO(savedTask);
        return dto;
    }

    /**
     * Get all tasks for the currently logged-in user with pagination.
     */
    @Override
    public Page<TaskDTO> getAllTasksForUser(Principal principal, int page, int size) {
        User user = userRepository.findByUsername(principal.getName());
        Pageable pageable = PageRequest.of(page, size, Sort.by("deadline").ascending());

        Page<Task> tasksPage = taskRepository.findByUserIdAndIsDeletedFalse(user.getId(), pageable);

        return tasksPage.map(task -> TaskConverter.convertToDTO(task));
    }

    /**
     * Filter the user's tasks based on status, priority, and keyword.
     */
    @Override
    public Page<TaskDTO> filterTasks(Principal principal, Status status, Priority priority, String keyword, int page, int size) {
        User user = userRepository.findByUsername(principal.getName());
        if (user == null) {
            throw new RuntimeException("User does not exist");
        }

        // Preprocess keyword
        String keywordProcessed = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Page<Task> tasksPage = taskRepository.filterTasksByConditions(
                user.getId(), status, priority, keywordProcessed, pageable);

        return tasksPage.map(task -> TaskConverter.convertToDTO(task));
    }

    /**
     * Get a single task by ID with permission checking.
     */
    @Override
    public TaskDTO getTaskById(Long id, Principal principal) throws IllegalAccessException, NoSuchElementException {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) {
            throw new NoSuchElementException("Task does not exist");
        }

        Task task = optionalTask.get();
        User currentUser = userRepository.findByUsername(principal.getName());

        // Permission check: must be owner or admin
        if (!task.getUserId().equals(currentUser.getId()) && !isAdmin()) {
            throw new IllegalAccessException("No permission to access this task");
        }

        TaskDTO dto = TaskConverter.convertToDTO(task);
        return dto;
    }

    /**
     * Update an existing task, only allowed for the owner or admin.
     */
    @Override
    public TaskDTO updateTask(Long id, Task updatedTask, Principal principal) throws IllegalAccessException, NoSuchElementException {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) {
            throw new NoSuchElementException("Task does not exist");
        }

        Task existingTask = optionalTask.get();
        User currentUser = userRepository.findByUsername(principal.getName());

        // Permission check
        if (!existingTask.getUserId().equals(currentUser.getId()) && !isAdmin()) {
            throw new IllegalAccessException("No permission to update this task");
        }

        // Update fields
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setDeadline(updatedTask.getDeadline());
        existingTask.setUpdatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(existingTask);

        TaskDTO dto = TaskConverter.convertToDTO(saved);
        return dto;
    }

    /**
     * Soft-delete a task (mark it as deleted), only allowed for the owner or admin.
     */
    @Override
    public void deleteTask(Long id, Principal principal) throws IllegalAccessException, NoSuchElementException {
        Optional<Task> optionalTask = taskRepository.findById(id);
        if (optionalTask.isEmpty()) {
            throw new NoSuchElementException("Task does not exist");
        }

        Task task = optionalTask.get();
        User user = userRepository.findByUsername(principal.getName());

        // Permission check
        if (!task.getUserId().equals(user.getId()) && !isAdmin()) {
            throw new IllegalAccessException("No permission to delete this task");
        }

        // Mark as deleted
        task.setDeleted(true);
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    /**
     * Utility method to determine if the current user has the ADMIN role.
     */
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

}


