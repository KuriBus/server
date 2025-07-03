package com.chatting.capstone.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 수신용 prefix (구독)
        config.enableSimpleBroker("/topic", "/queue");

        // 메시지 송신용 prefix (컨트롤러 @MessageMapping 경로)
        config.setApplicationDestinationPrefixes("/chat", "/app");

        // convertAndSendToUser 를 사용할 때 필요
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/app")
            .setAllowedOrigins(
                    "http://localhost:63342",
                    "http://127.0.0.1:5500",    // 프론트엔드 로컬호스트
                    "https://kuriverse.com", // 프론트엔드 도메인
                    "https://darling-starlight-f4b806.netlify.app") // 프론트엔드 netlify 도메인
            .withSockJS(); // SockJS fallback 지원
    }
}
