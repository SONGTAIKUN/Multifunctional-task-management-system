package com.example.taskmanager.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.taskmanager.dto.TaskDTO;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Task.Priority;
import com.example.taskmanager.model.Task.Status;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.TaskMapper;
import com.example.taskmanager.repository.UserMapper;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.util.TaskConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of TaskService.
 * 
 * Handles business logic for tasks, including:
 * - Creation
 * - Retrieval (single and paginated)
 * - Filtering
 * - Updating
 * - Soft deletion
 */
@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskMapper taskMapper;

    /**
     * Create a new task for the currently authenticated user.
     *
     * @param task      Task object containing task data
     * @param principal the authenticated user principal
     * @return created TaskDTO
     */
    @Override
    public TaskDTO createTask(Task task, Principal principal) {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .lambda()
                .eq(User::getUsername, principal.getName()));
        if (user == null) {
            throw new RuntimeException("User does not exist");
        }

        task.setUserId(user.getId());
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        task.setDeleted(false);

        taskMapper.insert(task); 

        return TaskConverter.convertToDTO(task);
    }

    /**
     * Get all tasks for the currently authenticated user with pagination.
     *
     * @param principal current authenticated user
     * @param page      page number (0-based)
     * @param size      number of records per page
     * @return Page of TaskDTOs
     */
    @Override
    public Page<TaskDTO> getAllTasksForUser(Principal principal, int page, int size) {

        User user = userMapper.selectOne(new QueryWrapper<User>()
                .lambda()
                .eq(User::getUsername, principal.getName()));

        Pageable pageable = PageRequest.of(page, size, Sort.by("deadline").ascending());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Task> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page + 1, size);

        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, user.getId())
                .eq(Task::getDeleted, false)
                .orderByAsc(Task::getDeadline);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Task> result =
                taskMapper.selectPage(mpPage, qw);

        List<TaskDTO> dtoList = result.getRecords()
                .stream().map(TaskConverter::convertToDTO).collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, result.getTotal());
    }

    /**
     * Filter tasks for the current user by status, priority, and keyword.
     *
     * @param principal current authenticated user
     * @param status    task status filter
     * @param priority  task priority filter
     * @param keyword   keyword to search in title/description
     * @param page      page number (0-based)
     * @param size      number of records per page
     * @return Page of TaskDTOs
     */
    @Override
    public Page<TaskDTO> filterTasks(Principal principal, Status status, Priority priority, String keyword, int page, int size) {
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .lambda()
                .eq(User::getUsername, principal.getName()));
        if (user == null) {
            throw new RuntimeException("User does not exist");
        }

        String kw = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Task> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page + 1, size);

        LambdaQueryWrapper<Task> qw = new LambdaQueryWrapper<Task>()
                .eq(Task::getUserId, user.getId())
                .eq(Task::getDeleted, false)
                .orderByDesc(Task::getCreatedAt);

        if (status != null) {
            qw.eq(Task::getStatus, status);
        }
        if (priority != null) {
            qw.eq(Task::getPriority, priority);
        }
        if (kw != null) {
            qw.and(w -> w.like(Task::getTitle, kw).or().like(Task::getDescription, kw));
        }

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Task> result =
                taskMapper.selectPage(mpPage, qw);

        List<TaskDTO> dtoList = result.getRecords()
                .stream().map(TaskConverter::convertToDTO).collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, result.getTotal());
    }

    /**
     * Get a single task by ID with permission checking.
     *
     * @param id        task ID
     * @param principal current authenticated user
     * @return TaskDTO
     * @throws IllegalAccessException   if user does not have permission
     * @throws NoSuchElementException   if task not found
     */
    @Override
    public TaskDTO getTaskById(Long id, Principal principal) throws IllegalAccessException, NoSuchElementException {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new NoSuchElementException("Task does not exist");
        }

        User currentUser = userMapper.selectOne(new QueryWrapper<User>()
                .lambda()
                .eq(User::getUsername, principal.getName()));

        if (!Objects.equals(task.getUserId(), currentUser.getId()) && !isAdmin()) {
            throw new IllegalAccessException("No permission to access this task");
        }

        return TaskConverter.convertToDTO(task);
    }

    /**
     * Update an existing task (only allowed for owner or admin).
     *
     * @param id          task ID
     * @param updatedTask updated task data
     * @param principal   current authenticated user
     * @return updated TaskDTO
     * @throws IllegalAccessException  if user does not have permission
     * @throws NoSuchElementException  if task not found
     */
    @Override
    public TaskDTO updateTask(Long id, Task updatedTask, Principal principal) throws IllegalAccessException, NoSuchElementException {
        Task existing = taskMapper.selectById(id);
        if (existing == null) {
            throw new NoSuchElementException("Task does not exist");
        }

        User currentUser = userMapper.selectOne(new QueryWrapper<User>()
                .lambda()
                .eq(User::getUsername, principal.getName()));

        if (!Objects.equals(existing.getUserId(), currentUser.getId()) && !isAdmin()) {
            throw new IllegalAccessException("No permission to update this task");
        }

        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setPriority(updatedTask.getPriority());
        existing.setStatus(updatedTask.getStatus());
        existing.setDeadline(updatedTask.getDeadline());
        existing.setUpdatedAt(LocalDateTime.now());

        taskMapper.updateById(existing);

        return TaskConverter.convertToDTO(existing);
    }

    /**
     * Soft-delete a task (mark as deleted). Only owner or admin can perform this action.
     *
     * @param id        task ID
     * @param principal current authenticated user
     * @throws IllegalAccessException  if user does not have permission
     * @throws NoSuchElementException  if task not found
     */
    @Override
    public void deleteTask(Long id, Principal principal) throws IllegalAccessException, NoSuchElementException {
        Task task = Optional.ofNullable(taskMapper.selectById(id))
                .orElseThrow(() -> new NoSuchElementException("Task does not exist"));

        User currentUser = userMapper.selectOne(new QueryWrapper<User>()
                .lambda()
                .eq(User::getUsername, principal.getName()));

        if (!Objects.equals(task.getUserId(), currentUser.getId()) && !isAdmin()) {
            throw new IllegalAccessException("No permission to delete this task");
        }

        task.setDeleted(true);
        task.setUpdatedAt(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    
    /**
     * Find tasks with deadlines between the given start and end time.
     *
     * @param start start datetime
     * @param end   end datetime
     * @return list of tasks within the deadline range
     */
    @Override
    public List<Task> findByDeadlineBetween(LocalDateTime start, LocalDateTime end) {
        return taskMapper.selectList(new QueryWrapper<Task>()
                .lambda()
                .between(Task::getDeadline, start, end)
                .eq(Task::getDeleted, false));
    }

    /**
     * Check if the current user has the ADMIN role.
     *
     * @return true if admin, false otherwise
     */
    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

}


