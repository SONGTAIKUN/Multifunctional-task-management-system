package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Task.Priority;
import com.example.taskmanager.model.Task.Status;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;

/**
 * Service interface for managing user tasks.
 * Provides operations to create, retrieve, filter, update, and delete tasks.
 */
public interface TaskService {

    /**
     * Creates a new task for the authenticated user.
     *
     * @param task      The task entity to be created.
     * @param principal The authenticated user information.
     * @return The created task as a DTO.
     */
    TaskDTO createTask(Task task, Principal principal);

    /**
     * Retrieves all tasks belonging to the authenticated user with pagination support.
     *
     * @param principal The authenticated user.
     * @param page      The page number (starting from 0).
     * @param size      The number of tasks per page.
     * @return A paginated list of task DTOs.
     */
    Page<TaskDTO> getAllTasksForUser(Principal principal, int page, int size);

    /**
     * Filters the user's tasks by status, priority, and keyword.
     *
     * @param principal The authenticated user.
     * @param status    The task status filter (optional).
     * @param priority  The task priority filter (optional).
     * @param keyword   Keyword to search in title/description (optional).
     * @param page      The page number.
     * @param size      The number of tasks per page.
     * @return A paginated list of filtered task DTOs.
     */
    Page<TaskDTO> filterTasks(Principal principal, Status status, Priority priority, String keyword, int page, int size);

    /**
     * Retrieves a task by its ID, ensuring the user has access to it.
     *
     * @param id        The ID of the task.
     * @param principal The authenticated user.
     * @return The corresponding task as a DTO.
     * @throws IllegalAccessException   If the user does not have permission.
     * @throws NoSuchElementException   If the task does not exist.
     */
    TaskDTO getTaskById(Long id, Principal principal) throws IllegalAccessException, NoSuchElementException;

    /**
     * Updates a task with new information, ensuring the user has permission.
     *
     * @param id           The ID of the task to update.
     * @param updatedTask  The task object with updated data.
     * @param principal    The authenticated user.
     * @return The updated task as a DTO.
     * @throws IllegalAccessException   If the user does not have permission.
     * @throws NoSuchElementException   If the task does not exist.
     */
    TaskDTO updateTask(Long id, Task updatedTask, Principal principal) throws IllegalAccessException, NoSuchElementException;

    /**
     * Updates a task with new information, ensuring the user has permission.
     *
     * @param id           The ID of the task to update.
     * @param updatedTask  The task object with updated data.
     * @param principal    The authenticated user.
     * @return The updated task as a DTO.
     * @throws IllegalAccessException   If the user does not have permission.
     * @throws NoSuchElementException   If the task does not exist.
     */
    void deleteTask(Long id, Principal principal) throws IllegalAccessException, NoSuchElementException;

    /** ✅ New: For the scheduler to query "tasks due today" */
    List<Task> findByDeadlineBetween(LocalDateTime start, LocalDateTime end);
}