package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.exception.NotYourEventException;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.model.EventTarget;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import com.marcinsz.eventmanagementsystem.request.JoinEventRequest;
import com.marcinsz.eventmanagementsystem.request.UpdateEventRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final KafkaMessageProducer kafkaMessageProducer;
    private final JwtService jwtService;


    @CacheEvict(cacheNames = "events", allEntries = true)
    @Transactional
    public EventDto createEvent(CreateEventRequest createEventRequest, User user) {
        Event event = EventMapper.convertCreateEventRequestToEvent(createEventRequest);
        event.setOrganizer(user);
        eventRepository.save(event);
        EventDto eventDto = EventMapper.convertCreateEventRequestToEventDto(createEventRequest,user, event.getId());
        kafkaMessageProducer.sendCreatedEventMessageToAllEventsTopic(eventDto);
        return eventDto;
    }

    @CacheEvict(cacheNames = "events",allEntries = true)
    public EventDto updateEvent(UpdateEventRequest updateEventRequest,Long eventId,String token) {
        Event foundEvent = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        String organiserUsername = foundEvent.getOrganizer().getUsername();
        String usernameExtractedFromToken = jwtService.extractUsername(token);

        String eventName = updateEventRequest.getEventName();
        String eventDescription = updateEventRequest.getEventDescription();
        String location = updateEventRequest.getLocation();
        Integer maxAttendees = updateEventRequest.getMaxAttendees();
        LocalDate eventDate = updateEventRequest.getEventDate();
        Double ticketPrice = updateEventRequest.getTicketPrice();
        EventTarget eventTarget = updateEventRequest.getEventTarget();

        if(!organiserUsername.equals(usernameExtractedFromToken)){
            throw new NotYourEventException();
        }

        if(eventName != null && !eventName.isEmpty()){
            foundEvent.setEventName(eventName);
        }
        if(eventDescription != null && !eventDescription.isEmpty()){
            foundEvent.setEventDescription(eventDescription);
        }
        if(location != null && !location.isEmpty()){
            foundEvent.setLocation(location);
        }
        if(maxAttendees != null){
            foundEvent.setMaxAttendees(maxAttendees);
        }
        if(eventDate != null){
            foundEvent.setEventDate(eventDate);
        }
        if(ticketPrice != null){
            foundEvent.setTicketPrice(ticketPrice);
        }
        if(eventTarget != null){
            foundEvent.setEventTarget(eventTarget);
        }

        foundEvent.setModifiedDate(LocalDateTime.now());

        eventRepository.save(foundEvent);
        return EventMapper.convertEventToEventDto(foundEvent);
    }

    @Cacheable(cacheNames = "events")
    public List<EventDto> showAllOrganizerEvents(String username){
        User user = userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
        List<Event> allByOrganizer = eventRepository.findAllByOrganizer(user);
        return EventMapper.convertListEventToListEventDto(allByOrganizer);

    }

    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public void joinEvent(JoinEventRequest joinEventRequest, String eventName,String token) {

        String username = jwtService.extractUsername(token);
        User foundUserByToken = userRepository.findByUsername(username).orElseThrow();
        if(!foundUserByToken.getEmail().equals(joinEventRequest.email)){
            throw new IllegalArgumentException("You can use your email only!");
        }
        Event foundEvent = eventRepository.findByEventName(eventName).orElseThrow(() -> new EventNotFoundException(eventName));
        User user = userRepository.findByEmail(joinEventRequest.getEmail()).orElseThrow(() -> UserNotFoundException.forEmail(joinEventRequest.email));

        UserMapper.convertUserToUserDto(user);
        if(foundEvent.getParticipants().contains(user)){
            throw new IllegalArgumentException("You already joined to this event!");
        } else if (!isUserAdult(user.getBirthDate())) {
            throw new IllegalArgumentException("You are too young to join this event!");
        } else if (foundEvent.getEventStatus().equals(EventStatus.COMPLETED)) {
            throw new IllegalArgumentException("Sorry, this event is full.");
        } else if (foundEvent.getEventStatus().equals(EventStatus.CANCELLED)) {
            throw new IllegalArgumentException("You cannot join to the event because this event has been cancelled.");
        }
        foundEvent.getParticipants().add(user);

        if(foundEvent.getParticipants().size() == foundEvent.getMaxAttendees()){
            foundEvent.setEventStatus(EventStatus.COMPLETED);
        }
        eventRepository.save(foundEvent);
    }

    @CacheEvict(cacheNames = "events",allEntries = true)
    @Transactional
    public String deleteEvent(Long eventId, String token) {
        Event eventToDelete = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        String organiserUsername = eventToDelete.getOrganizer().getUsername();
        String usernameLoggedUser = jwtService.extractUsername(token);
        String eventName = eventToDelete.getEventName();
        EventDto eventDto = EventMapper.convertEventToEventDto(eventToDelete);
        eventDto.setEventStatus(EventStatus.CANCELLED);
        kafkaMessageProducer.sendCancelledMessageToEventCancelledTopic(eventDto);
        if(!usernameLoggedUser.equals(organiserUsername)){
            throw new IllegalArgumentException("You can delete your events only!");
        }
        eventRepository.deleteById(eventId);
        return eventName;
    }

    boolean isUserAdult(LocalDate dateOfBirth){
        return dateOfBirth.isBefore(LocalDate.now().minusYears(18));
    }
    public User findByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow();
    }

}
