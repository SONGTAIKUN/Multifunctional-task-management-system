package com.example.taskmanager.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.taskmanager.model.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * MyBatis-Plus Mapper interface for the User entity.
 *
 * By extending BaseMapper, this interface provides built-in CRUD methods such as:
 * - selectById
 * - insert
 * - updateById
 * - deleteById
 *
 * If custom SQL queries are required, you can add methods here with 
 * annotations like @Select, @Update, @Delete, or @Insert.
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // Define custom SQL methods here if needed
}
