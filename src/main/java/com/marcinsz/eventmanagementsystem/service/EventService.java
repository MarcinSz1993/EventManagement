package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.exception.EventValidateException;
import com.marcinsz.eventmanagementsystem.exception.NotYourEventException;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import com.marcinsz.eventmanagementsystem.request.JoinEventRequest;
import com.marcinsz.eventmanagementsystem.request.UpdateEventRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final KafkaMessageProducer kafkaMessageProducer;
    private final JwtService jwtService;

    //todo wdrożyć mechanizm do automatycznego śledzenia utworzonych eventów i użytkowników
    //todo za pomocą @EntityListeners. (@CreatedBy, @ModifiedBy)

    public List<EventDto> getJoinedEvents(Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        return eventRepository.getEventsJoinedByUser(user.getId())
                 .stream()
                 .map(EventMapper::convertEventToEventDto)
                 .toList();
    }


    @CacheEvict(cacheNames = "events", allEntries = true)
    @Transactional
    public EventDto createEvent(CreateEventRequest createEventRequest, User user) {
        if(eventRepository.existsByEventName(createEventRequest.getEventName())){
            throw new EventValidateException(String.format("Event with name %s already exists!", createEventRequest.getEventName()));
        }
        Event event = EventMapper.convertCreateEventRequestToEvent(createEventRequest);
        event.setOrganizer(user);
        eventRepository.save(event);
        EventDto eventDto = EventMapper.convertCreateEventRequestToEventDto(createEventRequest,user, event.getId());
        kafkaMessageProducer.sendCreatedEventMessageToAllEventsTopic(eventDto);
        return eventDto;
    }

    public EventDto getEventById(Long eventId){
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        return EventMapper.convertEventToEventDto(event);
    }

    @CacheEvict(cacheNames = "events",allEntries = true)
    public EventDto updateEvent(UpdateEventRequest updateEventRequest,Long eventId,String token) {
        Event foundEvent = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        String organiserUsername = foundEvent.getOrganizer().getUsername();
        String usernameExtractedFromToken = jwtService.extractUsername(token);

        if(!organiserUsername.equals(usernameExtractedFromToken)){
            throw new NotYourEventException();
        }

        String eventName = updateEventRequest.getEventName();
        if(eventName != null && !eventName.isEmpty()){
            foundEvent.setEventName(eventName);
        }
        String eventDescription = updateEventRequest.getEventDescription();
        if(eventDescription != null && !eventDescription.isEmpty()){
            foundEvent.setEventDescription(eventDescription);
        }
        String location = updateEventRequest.getLocation();
        if(location != null && !location.isEmpty()){
            foundEvent.setLocation(location);
        }
        Integer maxAttendees = updateEventRequest.getMaxAttendees();
        if(maxAttendees != null){
            foundEvent.setMaxAttendees(maxAttendees);
        }
        LocalDate eventDate = updateEventRequest.getEventDate();
        if(eventDate != null){
            foundEvent.setEventDate(eventDate);
        }
        Double ticketPrice = updateEventRequest.getTicketPrice();
        if(ticketPrice != null){
            foundEvent.setTicketPrice(ticketPrice);
        }
        EventTarget eventTarget = updateEventRequest.getEventTarget();
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

    @Cacheable(cacheNames = "allEvents")
    public PageResponse<EventDto> showAllEvents(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Event> allEvents = eventRepository.findAll(pageable);
        List<EventDto> eventsDtoList = allEvents.stream()
                .map(EventMapper::convertEventToEventDto)
                .toList();
        return new PageResponse<>(
                eventsDtoList,
                allEvents.getNumber(),
                allEvents.getSize(),
                allEvents.getNumberOfElements(),
                allEvents.getTotalPages(),
                allEvents.isFirst(),
                allEvents.isLast()
        );
    }

    @Transactional
    @CacheEvict(value = "events", allEntries = true)
    public void joinEvent(JoinEventRequest joinEventRequest, String eventName,String token) {
        String extractedToken = token.substring(7);

        String username = jwtService.extractUsername(extractedToken);
        User foundUserByToken = userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
        if(!foundUserByToken.getEmail().equals(joinEventRequest.email)){
            throw new IllegalArgumentException("You can use your email only!");
        }
        Event foundEvent = eventRepository.findByEventName(eventName).orElseThrow(() -> new EventNotFoundException(eventName));
        User user = userRepository.findByEmail(joinEventRequest.getEmail()).orElseThrow(() -> UserNotFoundException.forEmail(joinEventRequest.email));

        UserMapper.convertUserToUserDto(user);
        if(foundEvent.getParticipants().contains(user)){
            throw new IllegalArgumentException("You already joined to this event!");
        } else if (!isUserAdult(user.getBirthDate()) && foundEvent.getEventTarget().equals(EventTarget.ADULTS_ONLY)) {
            throw new IllegalArgumentException("You are too young to join this event!");
        } else if (foundEvent.getEventStatus().equals(EventStatus.COMPLETED)) {
            throw new IllegalArgumentException("You can't join because this event has been finished.");
        } else if (foundEvent.getEventStatus().equals(EventStatus.CANCELLED)) {
            throw new IllegalArgumentException("You cannot join to the event because this event has been cancelled.");
        } else if (foundEvent.getEventStatus().equals(EventStatus.FULL)) {
            throw new IllegalArgumentException("You cannot join to the event because this is full.");
        }

        foundEvent.getParticipants().add(user);

        if(foundEvent.getParticipants().size() == foundEvent.getMaxAttendees()){
            foundEvent.setEventStatus(EventStatus.FULL);
        }
        eventRepository.save(foundEvent);
    }

    @CacheEvict(cacheNames = "events",allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public String deleteEvent(Long eventId, String token) {
        Event eventToDelete = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        String organiserUsername = eventToDelete.getOrganizer().getUsername();
        String usernameLoggedUser = jwtService.extractUsername(token);
        String eventName = eventToDelete.getEventName();
        EventDto eventDto = EventMapper.convertEventToEventDto(eventToDelete);
        eventDto.setEventStatus(EventStatus.CANCELLED);
        if(!usernameLoggedUser.equals(organiserUsername)){
            throw new IllegalArgumentException("You can delete your events only!");
        }
        eventRepository.deleteById(eventId);
        kafkaMessageProducer.sendCancelledEventMessageToCancellationTopic(eventDto);
        return eventName;
    }

    @Scheduled(cron = "0 32 13 * * ? ")
    public void updateEventsStatuses(){
        List<Event> activeEventsList = eventRepository.findAllByActiveEventStatus(EventStatus.ACTIVE);
        activeEventsList.forEach(event -> {
            if (event.getEventDate().isBefore(LocalDate.now())) {
                event.setEventStatus(EventStatus.COMPLETED);
            }
        });
        eventRepository.saveAll(activeEventsList);
        log.info("Event statuses have been updated");
    }

    boolean isUserAdult(LocalDate dateOfBirth){
        return dateOfBirth.isBefore(LocalDate.now().minusYears(18));
    }
    public User findByUsername(String username){
        return userRepository.findByUsername(username).orElseThrow();
    }

    public EventDto getEventByName(String eventName) {
        Event event = eventRepository.findByEventName(eventName).orElseThrow(() -> new EventNotFoundException(eventName));
        return EventMapper.convertEventToEventDto(event);
    }
}
