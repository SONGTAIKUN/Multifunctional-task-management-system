package com.example.taskmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfig {

    /**
     * Defines a custom RedisTemplate bean for Redis operations.
     * This template uses String keys and JSON-serialized values.
     *
     * @param factory the Redis connection factory (automatically injected by Spring)
     * @return a configured RedisTemplate
     */

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();

        // Set the connection factory (i.e., how to connect to Redis)
        template.setConnectionFactory(factory);

        // Use String serializer for all keys
        template.setKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for all values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Use String serializer for hash keys (e.g., in Redis hash structures)
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for hash values
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        // Finalize template configuration
        template.afterPropertiesSet();
        
        return template;
    }
}
