package com.chatting.capstone.domain.chat.redis;

import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisPublisher {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // ChatResponse용 기존 메서드
    public void publish(String topic, ChatResponse message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(topic, json);
        } catch (Exception e) {
            log.error("Redis publish error (ChatResponse)", e);
        }
    }

    // 문자열 메시지용 오버로딩 메서드
    public void publish(String topic, String message) {
        try {
            redisTemplate.convertAndSend(topic, message);
        } catch (Exception e) {
            log.error("Redis publish error (String)", e);
        }
    }
}
