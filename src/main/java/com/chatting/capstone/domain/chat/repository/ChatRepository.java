package com.chatting.capstone.domain.chat.repository;

import com.chatting.capstone.domain.chat.entity.Chat;
import com.chatting.capstone.domain.room.entity.Room;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findAllByRoomOrderByCreatedAtAsc(Room room);
}
