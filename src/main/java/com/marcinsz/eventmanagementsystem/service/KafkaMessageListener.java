package com.marcinsz.eventmanagementsystem.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.EventTarget;
import com.marcinsz.eventmanagementsystem.model.Ticket;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.TicketRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.TransactionKafkaRequest;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class KafkaMessageListener {

    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketRepository ticketRepository;

    @KafkaListener(topics = "${spring.kafka.config.allEventsTopic}", groupId = "${spring.kafka.config.bankServiceGroupId}")
    public void consumeCreateEventMessage(EventDto eventDto) throws MessagingException {
        log.info("Received EventDto: {}", eventDto);
        if (eventDto.getEventTarget().equals(EventTarget.ADULTS_ONLY)) {
            List<String> usersEmails = usersEmail(LocalDate.now());
            notificationService.sendNotification(eventDto);
            log.info("Sent email to: {}", Arrays.toString(usersEmails.toArray()));
        } else {
            log.info("Event different than for adults only.");
        }
    }

    @KafkaListener(topics = "${spring.kafka.config.cancelledEventsTopic}",groupId = "${spring.kafka.config.bankServiceGroupId}")
    public void consumeEventCancelledMessage(EventDto eventDto) throws MessagingException {
        notificationService.sendNotification(eventDto);
    }

    public List<String> usersEmail(LocalDate date){
        return userRepository.getEmailsFromAdultUsers(date);
    }

    @KafkaListener(topics = "${spring.kafka.config.completedPaymentsTopic}",groupId = "${spring.kafka.config.bankServiceGroupId}")
    public void handleExpectingPayment(String message) throws JsonProcessingException {
        System.out.println(message);
        ObjectMapper objectMapper = new ObjectMapper();
        TransactionKafkaRequest transactionKafkaRequest = objectMapper.readValue(message, TransactionKafkaRequest.class);
        User user = userRepository.findById(transactionKafkaRequest.getUserId()).orElseThrow(() -> new UserNotFoundException("User not found"));
        Event event = eventRepository.findById(transactionKafkaRequest.getEventId()).orElseThrow(() -> new EventNotFoundException("Event not found"));
        Ticket ticket = Ticket.builder()
                .event(event)
                .user(user)
                .hasTicket(true)
                .build();
        ticketRepository.save(ticket);
        log.info(String.format("Ticket for event %s has been purchased",event.getEventName()));
    }

    @KafkaListener(topics = "${spring.kafka.config.failedPaymentsTopic}",groupId = "${spring.kafka.config.bankServiceGroupId}")
    public void handleFailedTransactions(String message) throws JsonProcessingException, MessagingException {
        ObjectMapper objectMapper = new ObjectMapper();
        TransactionKafkaRequest transactionKafkaRequest = objectMapper.readValue(message, TransactionKafkaRequest.class);
        User user = userRepository.findById(transactionKafkaRequest.getUserId()).orElseThrow(() -> new UserNotFoundException("User not found"));
        log.info(String.format("Buying ticket for event %s failed due to %s",transactionKafkaRequest.getEventId(),transactionKafkaRequest.getReasonOfFail()));
        notificationService.sendInformationForUser(String.format("Buying ticket for event %d failed due to %s",transactionKafkaRequest.getEventId(),transactionKafkaRequest.getReasonOfFail()),user);
    }
}
