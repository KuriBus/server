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
        config.setApplicationDestinationPrefixes("/chat");

        // convertAndSendToUser 를 사용할 때 필요
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/app")
            .setHandshakeHandler(new CustomHandshakeHandler())
            .setAllowedOriginPatterns("*") // CORS 허용
            .withSockJS(); // SockJS fallback 지원
    }

    // 사용자 ID를 Principal 객체로 변환
    private static class CustomHandshakeHandler extends DefaultHandshakeHandler {
        @Override
        protected Principal determineUser(ServerHttpRequest request,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
            String query = request.getURI().getQuery(); // 예: ?userId=123

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] keyValue = param.split("=");
                    if (keyValue.length == 2 && keyValue[0].equals("userId")) {
                        String userId = keyValue[1];
                        return () -> userId;
                    }
                }
            }

            return null;
        }
    }
}
