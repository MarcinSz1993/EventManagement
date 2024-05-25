package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    public EventDto createEvent(CreateEventRequest createEventRequest,User user) {
        Event event = EventMapper.convertCreateEventRequestToEvent(createEventRequest);
        event.setOrganizer(user);
        eventRepository.save(event);
        return EventMapper.convertEventToEventDto(event);
    }

    public List<Event> showAllOrganizerEvents(String username){
        User user = userRepository.findByUsername(username).orElseThrow();
        return eventRepository.findAllByOrganizer(user);
    }
}
