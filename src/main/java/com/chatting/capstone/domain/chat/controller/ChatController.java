package com.chatting.capstone.domain.chat.controller;

import com.chatting.capstone.domain.chat.ai_ver.ClovaXClient;
import com.chatting.capstone.domain.chat.dto.request.ChatRequest;
import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.chatting.capstone.domain.chat.service.ChatService;
import com.chatting.capstone.global.moderation.ClovaService;
import com.chatting.capstone.global.response.CustomException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.chatting.capstone.global.response.ResponseStatus;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ClovaService clovaService;
    private final ClovaXClient clovaXClient;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    // 채팅 전송
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest dto, StompHeaderAccessor accessor) {
        String nickname = dto.getNickname();
        accessor.getSessionAttributes().put("nickname", nickname); // userId 대신 nickname

        try {
            validateMessage(dto.getContent());
            String originalContent = dto.getContent();
            long startTime = System.currentTimeMillis();

            String isInAppropriate = clovaService.appraiseSentence(originalContent);
            ChatResponse chatResponse;

            if ("1".equals(isInAppropriate)) {
                messagingTemplate.convertAndSend("/queue/warnings/" + nickname,
                    "⚠️ 부적절한 표현이 감지되어 자동으로 수정되었습니다.");

                CompletableFuture<String> filteredMessageFuture = clovaXClient.filterMessageAsync(originalContent);
                String filteredContent = filteredMessageFuture.get();
                chatResponse = chatService.save(dto, filteredContent);
            } else {
                chatResponse = chatService.save(dto, originalContent);
            }

            long elapsedTime = System.currentTimeMillis() - startTime;
            long minDelay = 100;
            if (elapsedTime < minDelay) {
                Thread.sleep(minDelay - elapsedTime);
            }

            messagingTemplate.convertAndSend("/topic/room/" + dto.getRoomId(), chatResponse);

        } catch (IllegalArgumentException e) {
            logger.error("빈 메시지 수신: {}", e.getMessage());
            sendErrorToUser(nickname, "메시지 전송 실패: " + e.getMessage());
        } catch (Exception e) {
            logger.error("채팅 전송 중 예외 발생: {}", e.getMessage());
            sendErrorToUser(nickname, "채팅 전송 중 오류가 발생했습니다.");
        }
    }

    //사용자에게 에러 메세지 전송
    private void sendErrorToUser(String nickname, String errorMessage) {
        messagingTemplate.convertAndSend("/queue/errors/" + nickname, errorMessage);
    }

    // 메시지 유효성 검사
    private void validateMessage(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new CustomException(ResponseStatus.INVALID_MESSAGE);
        }
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
