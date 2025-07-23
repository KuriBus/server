package com.chatting.capstone.domain.chat.controller;

import com.chatting.capstone.domain.chat.dto.request.ChatRequest;
import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.chatting.capstone.domain.chat.service.ChatService;
import com.chatting.capstone.global.response.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
import com.chatting.capstone.global.response.ResponseStatus;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;


    // 채팅 전송
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest dto, StompHeaderAccessor accessor) {
        String nickname = dto.getNickname();
        accessor.getSessionAttributes().put("nickname", nickname);

        long startTime = System.currentTimeMillis();

        chatService.processMessage(dto, nickname)
            .doOnSuccess(response -> sendWithDelay(dto.getRoomId(), response, startTime))
            .doOnError(e -> {
                log.error("채팅 처리 중 에러: {}", e.getMessage());
                String errMsg = switch (e instanceof CustomException ce ? ce.getResponseStatus() : ResponseStatus.SERVER_ERROR) {
                    case MUTED -> "⛔ 현재 도배로 인해 채팅이 30초간 정지되었습니다.";
                    case SPAM_DETECTED -> "⚠️ 도배로 판단되어 채팅이 30초간 제한됩니다.";
                    case INVALID_MESSAGE -> "메시지가 비어 있습니다.";
                    case MESSAGE_TOO_LONG -> "메시지가 너무 깁니다. 30자 이하로 작성해주세요.";
                    default -> "채팅 전송 중 오류가 발생했습니다.";
                };
                sendErrorToUser(nickname, errMsg);
            })
            .subscribe();
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

    // 채팅 기록 조회
    @Operation(summary = "채팅 기록 조회", description = "특정 방 ID에 해당하는 채팅 메시지 목록을 조회합니다.",
        parameters = {
            @Parameter(name = "roomId", description = "조회할 방의 ID", required = true, example = "1")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "채팅 목록 조회 성공"),
            @ApiResponse(responseCode = "401", description = "권한이 없음"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방 ID"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
        })
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
