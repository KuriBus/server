package com.chatting.capstone.domain.location.entity;

import com.chatting.capstone.domain.room.entity.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Portal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 포탈 고유 ID

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false) // Portal은 반드시 Room에 속해야 함
    @JoinColumn(name = "room_id", nullable = false) // 외래 키 컬럼 지정
    private Room room;

    // 기존 필드들
    private int portalX;
    private int portalY;
    private String targetRoomName;
    private int targetX;
    private int targetY;
}
