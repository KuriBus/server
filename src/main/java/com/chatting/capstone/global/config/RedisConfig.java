package com.chatting.capstone.global.config;

import com.chatting.capstone.domain.chat.redis.RedisSubscriber;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisSubscriber redisSubscriber;

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
        RedisConnectionFactory connectionFactory
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // 채팅방 관련 Topic (전체 구독)
        container.addMessageListener(redisSubscriber, new PatternTopic("chat:room:*"));

        // 사용자 경고 및 에러 알림
        container.addMessageListener(redisSubscriber, new PatternTopic("warnings:*"));
        container.addMessageListener(redisSubscriber, new PatternTopic("errors:*"));

        return container;
    }
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
