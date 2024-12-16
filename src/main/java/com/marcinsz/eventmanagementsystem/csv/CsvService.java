package com.marcinsz.eventmanagementsystem.csv;

import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.service.JwtService;
import com.marcinsz.eventmanagementsystem.service.KafkaMessageProducer;
import com.opencsv.exceptions.CsvValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CsvService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserCsvHandler userCsvHandler;
    private final EventCsvHandler eventCsvHandler;
    private final KafkaMessageProducer kafkaMessageProducer;

    @Transactional
    public Integer uploadUsers(MultipartFile file) throws IOException, CsvValidationException {
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            Set<UserCsvRepresentation> userCsvRepresentations = userCsvHandler.parse(reader, file);
            Set<User> users = userCsvRepresentations.stream()
                    .map(userCsvRepresentation -> {
                        User user = UserMapper.mapToUser(userCsvRepresentation);
                        user.setPassword(passwordEncoder.encode(user.getPassword()));
                        return user;
                    })
                    .collect(Collectors.toSet());
            userRepository.saveAll(users);
            return users.size();
        }
    }
    @Transactional
    public String uploadEvents(MultipartFile file, String token) throws IOException, CsvValidationException {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        try (Reader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            Set<EventCsvRepresentation> eventCsvRepresentations = eventCsvHandler.parse(reader, file);
            Set<Event> events = eventCsvRepresentations.stream().map(eventCsvRepresentation -> {
                        Event event = EventMapper.mapToEvent(eventCsvRepresentation);
                        event.setOrganizer(user);
                        return event;
                    })
                    .collect(Collectors.toSet());
            eventRepository.saveAll(events);
            sendEventToKafka(events);
            return "Added " + events.size() + " events";
        }
    }

    private void sendEventToKafka(Set<Event> events) {
        events.stream()
                .map(EventMapper::convertEventToEventDto)
                .forEach(kafkaMessageProducer::sendCreatedEventMessageToAllEventsTopic);
    }
}
