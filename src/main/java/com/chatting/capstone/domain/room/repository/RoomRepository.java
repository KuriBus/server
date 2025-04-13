package com.chatting.capstone.domain.room.repository;

import com.chatting.capstone.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
    boolean existsByRoomName(String roomName);

}
