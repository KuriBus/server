package com.chatting.capstone.domain.chat.service;


import com.chatting.capstone.domain.chat.dto.request.ChatRequest;
import com.chatting.capstone.domain.chat.dto.response.ChatResponse;
import com.chatting.capstone.domain.chat.entity.Chat;
import com.chatting.capstone.domain.chat.repository.ChatRepository;
import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
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
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));

        User user = userRepository.findByNickname(dto.getNickname())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

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
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 방입니다."));
        return chatRepository.findAllByRoomOrderByCreatedAtAsc(room).stream()
            .map(chat -> ChatResponse.builder()
                .id(chat.getId())
                .userId(chat.getUser().getId())  //userid도 같이 반환되게
                .nickname(chat.getNickname())
                .content(chat.getFilteredContent()) //필터링된 문장만 출력
                .createdAt(chat.getCreatedAt())
                .build())
            .collect(Collectors.toList());
    }
}
