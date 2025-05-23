package com.chatting.capstone.domain.customization.entity;

import com.chatting.capstone.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Getter
@Setter
@Table(name = "customization")
public class Customization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private int hairType;
    private int outfitType;

}
