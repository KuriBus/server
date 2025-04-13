package com.chatting.capstone.global.config;

import com.chatting.capstone.domain.room.entity.Room;
import com.chatting.capstone.domain.room.repository.RoomRepository;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomInitializer implements CommandLineRunner {
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public void run(String... args) {
        createRoomIfNotExists("Room 1");
        createRoomIfNotExists("Room 2");
        createRoomIfNotExists("Room 3");
    }

    private void createRoomIfNotExists(String roomName) {
        if (!roomRepository.existsByRoomName(roomName)) {
            roomRepository.save(
                    Room.builder()
                            .roomName(roomName)
                            .users(new ArrayList<>())
                            .build()
            );
        }
    }
}
