package com.example.taskmanager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.taskmanager.model.Task;
import org.apache.ibatis.annotations.Mapper;


/**
 * MyBatis-Plus Mapper interface for the Task entity.
 *
 * By extending BaseMapper, this interface automatically provides 
 * basic CRUD operations such as:
 * - selectById
 * - insert
 * - updateById
 * - deleteById
 *
 * Custom SQL queries can be defined here if additional functionality is required.
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    // Built-in CRUD methods are already available via BaseMapper
}
