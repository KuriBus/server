package com.chatting.capstone.domain.customization.service;

import com.chatting.capstone.domain.customization.dto.request.CustomizationRequest;
import com.chatting.capstone.domain.customization.dto.response.CustomizationResponse;
import com.chatting.capstone.domain.customization.entity.Customization;
import com.chatting.capstone.domain.customization.repository.CustomizationRepository;
import com.chatting.capstone.domain.user.entity.User;
import com.chatting.capstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.chatting.capstone.global.response.CustomException;
import com.chatting.capstone.global.response.ResponseStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomizationService {

    private final CustomizationRepository customizationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public void updateCustomization(CustomizationRequest customizationRequest) {
        User user = userRepository.findByNickname(customizationRequest.getNickname())
                .orElseThrow(() -> new CustomException(ResponseStatus.USER_NOT_FOUND));

        Customization customization = customizationRepository.findByUser(user)
            .orElse(new Customization());

        customization.setUser(user);
        customization.setBodyType(customizationRequest.getBodyType());
        customization.setNickname(customizationRequest.getNickname());

        customizationRepository.save(customization);

        // WebSocket 브로드캐스트
        CustomizationResponse response = new CustomizationResponse(
            user.getNickname(),
            customization.getBodyType()
        );
        System.out.println("Broadcasting customization update: " + response);
        messagingTemplate.convertAndSend("/topic/customization", response);
    }
}
