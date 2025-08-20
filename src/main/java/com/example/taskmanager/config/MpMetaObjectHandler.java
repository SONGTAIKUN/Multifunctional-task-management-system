package com.example.taskmanager.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus global auto-fill handler.
 *
 * This class automatically populates common audit fields 
 * during insert and update operations:
 * - On insert: sets createdAt, updatedAt, and timestamp
 * - On update: updates updatedAt
 */
@Component
public class MpMetaObjectHandler implements MetaObjectHandler {

  /**
   * Automatically fill fields during INSERT operations.
   * 
   * @param metaObject the MyBatis MetaObject representing the entity
   */
  @Override
  public void insertFill(MetaObject metaObject) {
    LocalDateTime now = LocalDateTime.now();
    // Set creation time
    strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
    // Set last updated time (same as creation for new records)
    strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    // Set a general timestamp field
    strictInsertFill(metaObject, "timestamp", LocalDateTime.class, now);
  }

  
  /**
   * Automatically fill fields during UPDATE operations.
   *
   * @param metaObject the MyBatis MetaObject representing the entity
   */
  @Override
  public void updateFill(MetaObject metaObject) {
    // Update the last updated time whenever a record is modified
    strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
  }
}
