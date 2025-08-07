package com.chatting.capstone.domain.chat.redis;

import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String topic = new String(message.getChannel());
            String body = new String(message.getBody());

            log.info("Redis 메시지 수신 topic={}, body={}", topic, body);

            // 채팅 메시지
            if (topic.startsWith("chat:room:")) {
                String roomId = topic.split(":")[2];
                ChatResponse chatResponse = objectMapper.readValue(body, ChatResponse.class);
                messagingTemplate.convertAndSend("/topic/room/" + roomId, chatResponse);
            }

            // 사용자 경고
            else if (topic.startsWith("warnings:")) {
                String nickname = topic.split(":")[1];
                messagingTemplate.convertAndSend("/queue/warnings/" + nickname, body); // ✅ 이 부분
            }

            // 사용자 에러
            else if (topic.startsWith("errors:")) {
                String nickname = topic.split(":")[1];
                messagingTemplate.convertAndSend("/queue/errors/" + nickname, body); // ✅ 이 부분
            }

        } catch (Exception e) {
            log.error("RedisSubscriber error", e);
        }
    }
}
