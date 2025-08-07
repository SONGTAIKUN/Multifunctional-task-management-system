package com.example.taskmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point of the Task Manager Spring Boot application.
 * 
 * Annotations:
 * - @SpringBootApplication: Enables component scanning, auto-configuration, and configuration support.
 * - @EnableConfigurationProperties: Allows usage of @ConfigurationProperties-annotated classes for external configuration binding.
 * - @EnableScheduling: Enables Spring’s scheduled task execution capability (e.g., for cron jobs).
 */
@SpringBootApplication
@EnableConfigurationProperties
@EnableScheduling  
public class TaskmanagerApplication {

    /**
     * The main method that launches the Spring Boot application.
     *
     * @param args Command-line arguments passed during application startup
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskmanagerApplication.class, args);
    }
}


