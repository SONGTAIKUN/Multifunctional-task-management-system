package com.example.taskmanager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.taskmanager.model.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus Mapper interface for the OperationLog entity.
 * 
 * Extending BaseMapper provides built-in CRUD methods such as:
 * - selectById
 * - insert
 * - updateById
 * - deleteById
 *
 * Custom SQL queries can be added here using annotations like 
 * @Select, @Update, @Delete, etc.
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
    // Define custom SQL methods here if needed
}
