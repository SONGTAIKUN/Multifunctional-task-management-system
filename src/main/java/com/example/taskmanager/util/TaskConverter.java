package com.example.taskmanager.util;

import com.example.taskmanager.model.Task;

import com.example.taskmanager.dto.TaskDTO;

/**
 * Utility class for converting Task entities to TaskDTO objects.
 * This helps to separate database entities from data transfer representations.
 */
public class TaskConverter {

    /**
     * Converts a Task entity into a TaskDTO.
     *
     * @param task The Task entity to convert
     * @return A TaskDTO containing the relevant data from the entity
     */
    public static TaskDTO convertToDTO(Task task) {

        // Copy basic task fields
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setPriority(task.getPriority());
        dto.setStatus(task.getStatus());
        dto.setDeadline(task.getDeadline());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setDeleted(task.getDeleted());

        // Set user ID and optionally the username (if user is loaded)
        dto.setUserId(task.getUserId());
        if (task.getUser() != null) {
            dto.setUsername(task.getUser().getUsername());
        }

        return dto;
    }
}
