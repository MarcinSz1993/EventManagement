package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.EventTarget;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import jakarta.mail.MessagingException;
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

    @KafkaListener(topics = "${spring.kafka.config.allEventsTopic}", groupId = "${spring.kafka.config.group-id}")
    public void consumeCreateEventMessage(EventDto eventDto) throws MessagingException {
        if(eventDto.getEventTarget().equals(EventTarget.ADULTS_ONLY)){
            notificationService.sendNotification(eventDto);
            log.info(String.valueOf(eventDto));
        } else {
            log.info("Event different than for adults only.");
        }
    }

    @KafkaListener(topics = "${spring.kafka.config.cancelledEventsTopic}",groupId = "${spring.kafka.config.group-id}")
    public void consumeEventCancelledMessage(EventDto eventDto) throws MessagingException {
        notificationService.sendNotification(eventDto);


    }

    public List<String> usersEmail(LocalDate date){
        return userRepository.getEmailsFromAdultUsers(date);

    }
}
