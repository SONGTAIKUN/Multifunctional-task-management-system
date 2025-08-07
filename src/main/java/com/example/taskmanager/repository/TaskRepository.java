package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.Task.Status;
import com.example.taskmanager.model.Task.Priority;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for performing database operations on Task entities.
 * Extends JpaRepository for basic CRUD operations.
 * Extends JpaSpecificationExecutor to allow dynamic query construction using Specifications.
 */
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * Retrieves a page of non-deleted tasks for a specific user.
     * 
     * @param userId   The ID of the user
     * @param pageable Pagination information
     * @return A page of tasks belonging to the user
     */
    Page<Task> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    /**
     * Retrieves all non-deleted tasks for a specific user (without pagination).
     * 
     * @param userId The ID of the user
     * @return A list of tasks belonging to the user
     */
    List<Task> findByUserIdAndIsDeletedFalse(Long userId);

    /**
     * Finds all non-deleted tasks whose deadline falls within the specified time range.
     * 
     * @param start Start of the deadline range
     * @param end   End of the deadline range
     * @return A list of tasks with deadlines within the given range
     */
    List<Task> findByDeadlineBetweenAndIsDeletedFalse(LocalDateTime start, LocalDateTime end);

    /**
     * Filters tasks for a specific user using optional criteria:
     * status, priority, and keyword (matched against title and description).
     * 
     * @param userId   ID of the user
     * @param status   Optional task status to filter by
     * @param priority Optional task priority to filter by
     * @param keyword  Optional keyword to search in title or description
     * @param pageable Pagination information
     * @return A page of tasks matching the criteria
     */
    @Query("SELECT t FROM Task t WHERE t.userId = :userId AND t.isDeleted = false"
         + " AND (:status IS NULL OR t.status = :status)"
         + " AND (:priority IS NULL OR t.priority = :priority)"
         + " AND (:keyword IS NULL OR (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))"
         + " OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<Task> filterTasksByConditions(
        @Param("userId") Long userId,
        @Param("status") Status status,
        @Param("priority") Priority priority,
        @Param("keyword") String keyword,
        Pageable pageable);

    /**
     * Admin-level filtering of all non-deleted tasks.
     * Supports optional filtering by task status, priority, keyword, and username.
     * 
     * @param status   Optional task status
     * @param priority Optional task priority
     * @param keyword  Optional keyword in title/description
     * @param username Optional username to filter tasks by user
     * @param pageable Pagination information
     * @return A page of tasks matching the specified filters
     */
    @Query("SELECT t FROM Task t " +
       "LEFT JOIN t.user u " +
       "WHERE t.isDeleted = false " +
       "AND (:status IS NULL OR t.status = :status) " +
       "AND (:priority IS NULL OR t.priority = :priority) " +
       "AND (:keyword IS NULL OR (" +
       "  LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
       "  OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
       "AND (:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')))")
    Page<Task> filterAllTasksByConditions(
        @Param("status") Status status,
        @Param("priority") Priority priority,
        @Param("keyword") String keyword,
        @Param("username") String username,
        Pageable pageable
    );

}
