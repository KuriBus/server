package com.chatting.capstone.domain.room.entity;

import com.chatting.capstone.domain.location.entity.Portal;
import com.chatting.capstone.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 방 ID

    @Column(nullable = false, unique = true)
    private String roomName; // 방 이름

    // 좌표 제한 및 포탈 위한 필드
    @Column(nullable = false)
    @Builder.Default
    private int width = 1600; // 방 가로 크기 (1600)

    @Column(nullable = false)
    @Builder.Default
    private int height = 900; // 방 세로 크기 (900)

    @OneToMany(mappedBy = "room", // Portal 엔티티의 'room' 필드에 의해 매핑됨
            cascade = CascadeType.ALL, // Room 저장/삭제 시 Portal도 함께 처리
            orphanRemoval = true)      // Room의 portals 리스트에서 제거되면 DB에서도 삭제
    @Builder.Default
    private List<Portal> portals = new ArrayList<>();

    @OneToMany(mappedBy = "room")
    private List<User> users = new ArrayList<>();

    // --- 중요: 연관관계 편의 메소드 (양방향 설정) ---
    public void addPortal(Portal portal) {
        this.portals.add(portal);
        portal.setRoom(this); // Portal 객체에도 Room 참조 설정
    }

    public void removePortal(Portal portal) {
        this.portals.remove(portal);
        portal.setRoom(null); // 참조 제거 (orphanRemoval=true 설정 시 DB에서도 삭제됨)
    }
}
