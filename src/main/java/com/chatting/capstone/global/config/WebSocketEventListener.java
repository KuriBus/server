package com.chatting.capstone.global.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);
    @Autowired
    private SimpMessagingTemplate messagingTemplate; // WebSocket 메시지 전송용

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");

        // 유저가 존재할 경우 로그 남기고 클라이언트로 연결 끊어졌다는 메시지 전송
        if (userId != null) {
            logger.warn("❗ 유저 '{}' 연결 해제됨", userId);

            // 연결 끊어졌음을 해당 유저에게 전송
            messagingTemplate.convertAndSendToUser(userId, "/queue/errors", "❌ 연결이 끊어졌습니다. 다시 연결해주세요.");
        } else {
            logger.warn("❗ 세션 ID '{}' 연결 해제됨", sessionId);
        }
    }
}
