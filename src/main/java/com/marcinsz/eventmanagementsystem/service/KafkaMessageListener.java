package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.EventType;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    @KafkaListener(topics = "allEvents", groupId = "events-group")
    public void consumeMessage(EventDto eventDto){
        if(eventDto.getEventType().equals(EventType.ADULTS_ONLY)){
            notificationService.sendNotification(eventDto);
            log.info(String.valueOf(eventDto));
        } else {
            log.info("Event different than for adults only.");
        }
    }

    public List<String> usersEmail(LocalDate date){
        return userRepository.getEmailsUsersBornBefore2006(date);

    }
}
