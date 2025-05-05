package com.chatting.capstone.domain.location.repository;

import com.chatting.capstone.domain.location.entity.Portal;
import com.chatting.capstone.domain.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PortalRepository extends JpaRepository<Portal, Long> {
    // 특정 방의 특정 좌표에 있는 포탈 조회
    Optional<Portal> findByRoomAndPortalXAndPortalY(Room room, int portalX, int portalY);
}
