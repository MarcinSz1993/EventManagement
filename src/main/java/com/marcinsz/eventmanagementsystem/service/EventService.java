package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import com.marcinsz.eventmanagementsystem.request.JoinEventRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

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

    @Transactional
    public void joinEvent(JoinEventRequest joinEventRequest, String eventName){
        Event foundEvent = eventRepository.findByEventName(eventName).orElseThrow();
        User user = userRepository.findByEmail(joinEventRequest.getEmail()).orElseThrow();
        if(foundEvent.getParticipants().contains(user)){
            throw new IllegalArgumentException("You already joined to this event!");
        } else if (foundEvent.getParticipants().size() >= foundEvent.getMaxAttendees()) {
            throw new IllegalArgumentException("Sorry, this event is full.");
        }
        foundEvent.getParticipants().add(user);
        eventRepository.save(foundEvent);
    }

    @Transactional
    public String deleteEvent(Long eventId, String token) {
        Event eventToDelete = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("The event with id " + eventId + " not found."));
        String organiserUsername = eventToDelete.getOrganizer().getUsername();
        String usernameLoggedUser = jwtService.extractUsername(token);
        String eventName = eventToDelete.getEventName();

        if(!usernameLoggedUser.equals(organiserUsername)){
            throw new IllegalArgumentException("You can delete your events only!");
        }
        eventRepository.deleteById(eventId);
        return eventName;
    }
}
