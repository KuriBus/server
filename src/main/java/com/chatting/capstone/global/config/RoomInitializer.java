package com.chatting.capstone.global.config;

import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomInitializer {

    private final RoomRepository roomRepository;

    @PostConstruct
    public void init() {
        createRoomIfNotExists("Room 1");
        createRoomIfNotExists("Room 2");
        createRoomIfNotExists("Room 3");
    }

    private void createRoomIfNotExists(String roomName) {
        boolean exists = roomRepository.existsByRoomName(roomName);
        if (!exists) {
            roomRepository.save(
                    Room.builder()
                            .roomName(roomName)
                            .users(new ArrayList<>())
                            .build()
            );
        }
    }
}
