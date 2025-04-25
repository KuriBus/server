package com.chatting.capstone.domain.chat.controller;

import com.chatting.capstone.domain.chat.ai_ver.ClovaXClient;
import com.chatting.capstone.domain.chat.dto.request.ChatRequest;
import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.chatting.capstone.domain.chat.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ClovaXClient clovaXClient;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Value("${clova.toxicity.threshold:0.6}") // 설정파일에서 임계값 주입 가능 (기본값 0.6)
    private double toxicityThreshold;

    // 채팅 전송
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest dto, StompHeaderAccessor accessor) {
        String userId = String.valueOf(dto.getUserId());
        accessor.getSessionAttributes().put("userId", userId);

        try {
            validateMessage(dto.getContent());
            String originalContent = dto.getContent(); // 🔹 원본 따로 보관

            clovaXClient.filterMessageAsync(originalContent, toxicityThreshold)
                .thenAccept(filteredContent -> {
                    ChatResponse response = chatService.save(dto, filteredContent);
                    messagingTemplate.convertAndSend("/topic/room/" + dto.getRoomId(), response);
                });

        } catch (IllegalArgumentException e) {
            logger.error("빈 메시지 수신: {}", e.getMessage());
            sendErrorToUser(userId, "메시지 전송 실패: " + e.getMessage());

        } catch (Exception e) {
            logger.error("채팅 전송 중 예외 발생: {}", e.getMessage());
            sendErrorToUser(userId, "채팅 전송 중 오류가 발생했습니다.");
        }
    }

    // 메시지 유효성 검사
    private void validateMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("빈 메시지는 보낼 수 없습니다.");
        }
    }

    // 사용자에게 에러 메시지 전송
    private void sendErrorToUser(String userId, String errorMessage) {
        messagingTemplate.convertAndSendToUser(userId, "/queue/errors", errorMessage);
    }

    // 채팅 기록 조회
    @GetMapping("/api/rooms/{roomId}/chats")
    public List<ChatResponse> getChatMessages(@PathVariable Long roomId) {
        return chatService.getChatsByRoom(roomId);
    }

    // WebSocket 연결 끊김 처리
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) accessor.getSessionAttributes().get("userId");

        if (userId != null) {
            logger.error("세션 종료: userId={} 연결 끊김", userId);
            sendErrorToUser(userId, "연결이 끊어졌습니다. 다시 연결을 시도해주세요.");
        }
    }

    // 전역 예외 처리 (WebSocket)
    @MessageExceptionHandler
    public void handleException(Exception e) {
        logger.error("WebSocket 메시지 처리 중 오류: ", e);
    }
}
