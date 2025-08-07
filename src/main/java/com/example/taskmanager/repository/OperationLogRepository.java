package com.example.taskmanager.repository;

import com.example.taskmanager.model.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for performing CRUD operations on OperationLog entities.
 * Extends JpaRepository to inherit standard database operations (save, findAll, delete, etc.).
 */
@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {
    // No additional methods are defined here yet.
    // JpaRepository provides all basic operations such as:
    // - save()
    // - findById()
    // - findAll()
    // - deleteById()
    // - and more...
}
