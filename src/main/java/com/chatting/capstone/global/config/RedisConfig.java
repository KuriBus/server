package com.chatting.capstone.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        // Key Serializer 설정 (String)
        template.setKeySerializer(new StringRedisSerializer());
        // Value Serializer 설정 (String) - 일반적인 값
        template.setValueSerializer(new StringRedisSerializer());
        // Hash Key Serializer 설정 (String)
        template.setHashKeySerializer(new StringRedisSerializer());
        // Hash Value Serializer 설정 (String)
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}
