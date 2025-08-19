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

    // 채팅 전송
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest dto, StompHeaderAccessor accessor) {
        String nickname = dto.getNickname();
        accessor.getSessionAttributes().put("nickname", nickname);
        System.out.println("Front에서 온 DTO: " + dto);
        long startTime = System.currentTimeMillis();

        try {
            chatService.processMessage(dto, nickname, startTime);
        } catch (CustomException e) {
            log.error("채팅 처리 중 예외 발생", e);
        } catch (Exception e) {
            log.error("알 수 없는 예외 발생", e);
        }
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
            chatService.publishErrorToUser(userId, "연결이 끊어졌습니다. 다시 연결을 시도해주세요.");
        }
    }

    // 전역 예외 처리 (WebSocket)
    @MessageExceptionHandler
    public void handleException(Exception e) {
        log.error("WebSocket 메시지 처리 중 오류: ", e);
    }
}
