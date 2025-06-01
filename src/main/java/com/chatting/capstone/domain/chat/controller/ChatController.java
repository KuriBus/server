package com.chatting.capstone.domain.chat.controller;

import com.chatting.capstone.domain.chat.ai_ver.ClovaXClient;
import com.chatting.capstone.domain.chat.dto.request.ChatRequest;
import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.chatting.capstone.domain.chat.service.ChatService;
import com.chatting.capstone.global.moderation.ClovaService;
import com.chatting.capstone.global.response.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import reactor.core.publisher.Mono;
import com.chatting.capstone.global.response.ResponseStatus;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ClovaService clovaService;
    private final ClovaXClient clovaXClient;

    // 채팅 전송
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest dto, StompHeaderAccessor accessor) {
        String nickname = dto.getNickname();
        accessor.getSessionAttributes().put("nickname", nickname); // userId 대신 nickname

        processMessage(dto, nickname);
    }

    private void processMessage(ChatRequest dto, String nickname) {
        long startTime = System.currentTimeMillis(); // 전체 처리 시작 시점
        try {
            validateMessage(dto.getContent());
            String originalContent = dto.getContent();

            long appraisalStart = System.currentTimeMillis(); // Clova appraisal 시작

            clovaService.appraiseSentence(originalContent)
                .flatMap(isInappropriate -> {
                    long appraisalEnd = System.currentTimeMillis(); // Clova appraisal 종료
                    log.info("[{}] Clova appraisal duration: {}ms", nickname, appraisalEnd - appraisalStart);

                    if ("1".equals(isInappropriate)) {
                        messagingTemplate.convertAndSend("/queue/warnings/" + nickname,
                            "⚠️ 부적절한 표현이 감지되어 자동으로 수정되었습니다.");

                        long clovaXStart = System.currentTimeMillis(); // ClovaX 시작

                        return clovaXClient.filterMessageAsync(originalContent)
                            .flatMap(filteredContent -> {
                                long clovaXEnd = System.currentTimeMillis();
                                long clovaXDuration = clovaXEnd - clovaXStart;
                                long totalDuration = clovaXEnd - startTime;

                                log.info("[{}] ClovaX filtering duration: {}ms", nickname, clovaXDuration);
                                log.info("[{}] Total time until message sent: {}ms", nickname, totalDuration);

                                ChatResponse response = chatService.save(dto, filteredContent);
                                return Mono.just(response);
                            });
                    } else {
                        long totalEnd = System.currentTimeMillis();
                        log.info("[{}] No filtering needed. Total time until message sent: {}ms", nickname, totalEnd - startTime);
                        return Mono.just(chatService.save(dto, originalContent));
                    }
                })
                .doOnSuccess(chatResponse -> sendWithDelay(dto.getRoomId(), chatResponse, startTime))
                .doOnError(e -> {
                    log.error("채팅 처리 중 비동기 예외 발생: {}", e.getMessage(), e);
                    sendErrorToUser(nickname, "채팅 전송 중 오류가 발생했습니다.");
                })
                .subscribe();

        } catch (IllegalArgumentException e) {
            log.error("빈 메시지 수신: {}", e.getMessage());
            sendErrorToUser(nickname, "메시지 전송 실패: " + e.getMessage());
        } catch (Exception e) {
            log.error("채팅 전송 중 예외 발생: {}", e.getMessage(), e);
            sendErrorToUser(nickname, "채팅 전송 중 오류가 발생했습니다.");
        }
    }

    private void sendWithDelay(Long roomId, ChatResponse chatResponse, long startTime) {
        long elapsedTime = System.currentTimeMillis() - startTime;
        long minDelay = 100;

        if (elapsedTime < minDelay) {
            try {
                Thread.sleep(minDelay - elapsedTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        messagingTemplate.convertAndSend("/topic/room/" + roomId, chatResponse);
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
            log.error("세션 종료: userId={} 연결 끊김", userId);
            sendErrorToUser(userId, "연결이 끊어졌습니다. 다시 연결을 시도해주세요.");
        }
    }

    // 전역 예외 처리 (WebSocket)
    @MessageExceptionHandler
    public void handleException(Exception e) {
        log.error("WebSocket 메시지 처리 중 오류: ", e);
    }
}
