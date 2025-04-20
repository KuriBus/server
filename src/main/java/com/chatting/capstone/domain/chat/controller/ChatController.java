package com.chatting.capstone.domain.chat.controller;

import com.chatting.capstone.domain.chat.dto.request.ChatRequest;
import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.chatting.capstone.domain.chat.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 전송용
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    // 채팅 전송
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest dto) {
        try {
            if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("빈 메시지는 보낼 수 없습니다.");    //채팅에 아무것도 없을 시 예외처리
            }

            ChatResponse response = chatService.save(dto);
            String destination = "/topic/room/" + dto.getRoomId();
            messagingTemplate.convertAndSend(destination, response);

        } catch (Exception e) {
            logger.error("채팅 전송 중 오류 발생", e);
            // 예외는 아래의 @MessageExceptionHandler에서 처리됨
            throw e;
        }
    }

    // 예외 처리: 클라이언트에게 전송
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        logger.warn("웹소켓 예외 처리됨: {}", exception.getMessage());
        return "에러 발생: " + exception.getMessage();
    }


    // 채팅 조회
    @GetMapping("/api/rooms/{roomId}/chats")
    public List<ChatResponse> getChatMessages(@PathVariable Long roomId) {
        return chatService.getChatsByRoom(roomId);
    }
}
