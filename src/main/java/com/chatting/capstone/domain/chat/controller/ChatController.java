package com.chatting.capstone.domain.chat.controller;

import com.chatting.capstone.domain.chat.dto.request.ChatRequest;
import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.chatting.capstone.domain.chat.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate; // WebSocket 전송용

    // 채팅 전송
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatRequest dto) {
        ChatResponse response = chatService.save(dto); // DB 저장
        String destination = "/topic/room/" + dto.getRoomId(); // 해당 room 채널로 전송
        messagingTemplate.convertAndSend(destination, response); // 채팅 전송
    }


    // 채팅 조회
    @GetMapping("/api/rooms/{roomId}/chats")
    public List<ChatResponse> getChatMessages(@PathVariable Long roomId) {
        return chatService.getChatsByRoom(roomId);
    }
}
