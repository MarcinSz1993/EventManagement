package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.dto.OrganiserDto;
import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;

    @Async
    public void sendNotification(EventDto eventDto) throws MessagingException {
        MimeMessage message = createNewMessage(eventDto);
        mailSender.send(message);
    }

    public void sendInformationForUser(String message, User user) throws MessagingException {
        MimeMessage informationForUser = createInformationForUser(message, user);
        mailSender.send(informationForUser);
    }

    public MimeMessage createInformationForUser(String message, User user) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage,true);
        Context context = setThymeleafContextForInformation(message);
        String htmlContent = templateEngine.process("email/InformationForUserNotification", context);
        String subject = "Info about payment";
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(htmlContent,true);
        mimeMessageHelper.addTo(user.getEmail());
        return mimeMessage;
    }

    public MimeMessage createNewMessage(EventDto eventDto) throws MessagingException {
        List<String> allUsersEmails = userRepository.getEmailsFromAdultUsers(LocalDate.now().minusYears(18));
        allUsersEmails.remove(eventDto.getOrganiser().getEmail());
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        Context context = setThymeleafContext(eventDto);
        String htmlContent = templateEngine.process("email/NewEventNotification", context);
        String subject = "New event for you is waiting " + eventDto.getEventName();
        if (eventDto.getEventStatus().equals(EventStatus.CANCELLED)) {
            htmlContent = templateEngine.process("email/EventCancelledNotification", context);
            subject = "Cancellation " + eventDto.getEventName().toUpperCase() + " event.";
        }
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        for (String allUsersEmail : allUsersEmails) {
            helper.addTo(allUsersEmail);
        }
        return message;
    }

    public Context setThymeleafContextForInformation(String message){
        Context context = new Context();
        context.setVariable("message", message);
        return context;
    }

    public Context setThymeleafContext(EventDto eventDto) {
        Context context = new Context();
        OrganiserDto organiser = eventDto.getOrganiser();

        context.setVariable("eventName", eventDto.getEventName());
        context.setVariable("eventDescription", eventDto.getEventDescription());
        context.setVariable("eventLocation", eventDto.getEventLocation());
        context.setVariable("eventDate", eventDto.getEventDate().toString());
        context.setVariable("organiserFirstName", organiser.getFirstName());
        context.setVariable("organiserLastName", organiser.getLastName());
        context.setVariable("organiserUserName", organiser.getUserName());
        context.setVariable("organiserPhoneNumber", organiser.getPhoneNumber());
        return context;
    }

}
