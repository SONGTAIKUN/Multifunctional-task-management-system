package com.example.taskmanager.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



/**
 * Configuration class for MyBatis Plus.
 * 
 * This class defines the necessary configuration for enabling
 * MyBatis Plus features such as pagination and mapper scanning.
 */
@Configuration
@MapperScan("com.example.taskmanager.repository")  
public class MyBatisPlusConfig {

  /**
   * Register MyBatis Plus interceptor.
   * 
   * The interceptor is used to add internal plugins for MyBatis Plus.
   * Here we add the PaginationInnerInterceptor to support pagination queries
   * for MySQL databases.
   *
   * @return MybatisPlusInterceptor instance with configured plugins
   */
  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

    // Add pagination support for MySQL database
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
  }
}
