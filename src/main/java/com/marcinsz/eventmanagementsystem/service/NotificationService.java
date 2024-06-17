package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    public void sendNotification(EventDto eventDto){
        List<String> allUsersEmails = userRepository.getAllUsersEmails();
        SimpleMailMessage message = new SimpleMailMessage();
        for (String allUsersEmail : allUsersEmails) {
            message.setTo(allUsersEmail);
            message.setSubject("New event for you is waiting: " + eventDto.getEventName());
            message.setText("Event details:  \n" +
                    "Name: " + eventDto.getEventName() + "\n" +
                    "Description: " + eventDto.getEventDescription() + "\n" +
                    "Location: " + eventDto.getEventLocation() + "\n" +
                    "Date: " + eventDto.getEventDate() + "\n" +
                    "Organiser: " + eventDto.getOrganiser().getFirstName() + " " +
                    eventDto.getOrganiser().getLastName() + "\n" +
                    eventDto.getOrganiser().getUserName() + "\n" +
                    "Tel: " + eventDto.getOrganiser().getPhoneNumber());
            mailSender.send(message);
            log.info("Sent e-mail to: " + allUsersEmail);
        }
    }

        }


