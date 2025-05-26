package com.chatting.capstone.global.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

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
                    "https://kuriverse.com") // CORS 허용 부분 정확히 지정
            .withSockJS(); // SockJS fallback 지원
    }
}
