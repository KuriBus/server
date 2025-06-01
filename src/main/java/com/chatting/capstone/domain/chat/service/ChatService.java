package com.chatting.capstone.domain.chat.service;

import com.chatting.capstone.domain.chat.dto.request.ChatRequest;
import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.chatting.capstone.domain.chat.entity.Chat;
import com.chatting.capstone.domain.chat.repository.ChatRepository;
import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public ChatResponse save(ChatRequest dto, String filteredContent) {
        Room room = roomRepository.findById(dto.getRoomId())
            .orElseThrow(() -> new CustomException(ResponseStatus.ROOM_NOT_FOUND));

        User user = userRepository.findByNickname(dto.getNickname())
            .orElseThrow(() -> new CustomException(ResponseStatus.USER_NOT_FOUND));

        Chat chat = Chat.builder()
            .room(room)
            .user(user)
            .nickname(dto.getNickname())
            .originalContent(dto.getContent())
            .filteredContent(filteredContent)
            .createdAt(LocalDateTime.now())
            .build();

        chatRepository.save(chat);

        return ChatResponse.builder()
            .id(chat.getId())
            .userId(chat.getUser().getId())
            .nickname(chat.getNickname())
            .content(chat.getFilteredContent())
            .createdAt(chat.getCreatedAt())
            .build();
    }

    public List<ChatResponse> getChatsByRoom(Long roomId) {
        return chatRepository.findAllByRoomIdOrderByCreatedAtAsc(roomId).stream()
            .map(chat -> ChatResponse.builder()
                .id(chat.getId())
                .userId(chat.getUser().getId())
                .nickname(chat.getNickname())
                .content(chat.getFilteredContent())
                .createdAt(chat.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }
}
