package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.exception.*;
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

    @Transactional
    public String leaveEvent(Long eventId, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Event eventUserWantsToLeave = eventRepository.findByIdAndUserId(eventId, user.getId())
                .orElseThrow(() -> new UserNotParticipantException("You are not a participant of this event"));
        validateEventAndUserBeforeExecuteLeaveEventMethod(eventUserWantsToLeave);
        eventUserWantsToLeave.getParticipants().removeIf(userToRemove -> userToRemove.getId().equals(user.getId()));
        if (eventUserWantsToLeave.getParticipants().size() < eventUserWantsToLeave.getMaxAttendees()) {
            eventUserWantsToLeave.setEventStatus(EventStatus.ACTIVE);
        }
        user.getEvents().removeIf(eventToRemove -> eventToRemove.getId().equals(eventId));
        return eventUserWantsToLeave.getEventName();
    }


    private static void validateEventAndUserBeforeExecuteLeaveEventMethod(Event event) {

        if (!event.getEventStatus().equals(EventStatus.ACTIVE) && !event.getEventStatus().equals(EventStatus.FULL)) {
            throw new IllegalStateException("Event is COMPLETED or CANCELLED");
        }
    }

    public PageResponse<EventDto> getCompletedJoinedEvents(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        Page<Event> joinedEvents = eventRepository.findCompletedEventsJoinedByUser(user.getId(), pageable);
        List<EventDto> joinedEventsDtoList = joinedEvents.getContent().stream()
                .map(EventMapper::convertEventToEventDto)
                .toList();

        return new PageResponse<>(
                joinedEventsDtoList,
                joinedEvents.getNumber(),
                joinedEvents.getSize(),
                joinedEvents.getNumberOfElements(),
                joinedEvents.getTotalPages(),
                joinedEvents.isFirst(),
                joinedEvents.isLast()
        );
    }

    public PageResponse<EventDto> getFullAndActiveJoinedEvents(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        Page<Event> joinedEvents = eventRepository.findFullAndActiveEventsJoinedByUser(user.getId(), pageable);
        List<EventDto> joinedEventsDtoList = joinedEvents.getContent().stream()
                .map(EventMapper::convertEventToEventDto)
                .toList();

        return new PageResponse<>(
                joinedEventsDtoList,
                joinedEvents.getNumber(),
                joinedEvents.getSize(),
                joinedEvents.getNumberOfElements(),
                joinedEvents.getTotalPages(),
                joinedEvents.isFirst(),
                joinedEvents.isLast()
        );
    }

    public PageResponse<EventDto> getAllJoinedEvents(int page, int size, Authentication connectedUser) {
        User user = (User) connectedUser.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").descending());
        Page<Event> joinedEvents = eventRepository.findFullAndActiveEventsJoinedByUser(user.getId(), pageable);
        List<EventDto> joinedEventsDtoList = joinedEvents.getContent().stream()
                .map(EventMapper::convertEventToEventDto)
                .toList();

        return new PageResponse<>(
                joinedEventsDtoList,
                joinedEvents.getNumber(),
                joinedEvents.getSize(),
                joinedEvents.getNumberOfElements(),
                joinedEvents.getTotalPages(),
                joinedEvents.isFirst(),
                joinedEvents.isLast()
        );
    }

    @CacheEvict(cacheNames = {"allEvents","organizerEvents"}, allEntries = true)
    @Transactional
    public EventDto createEvent(CreateEventRequest createEventRequest, User user) {
        if (eventRepository.existsByEventName(createEventRequest.getEventName())) {
            throw new EventValidateException(String.format("Event with name %s already exists!", createEventRequest.getEventName()));
        }
        Event event = EventMapper.convertCreateEventRequestToEvent(createEventRequest);
        event.setOrganizer(user);
        eventRepository.save(event);
        EventDto eventDto = EventMapper.convertCreateEventRequestToEventDto(createEventRequest, user, event.getId());
        kafkaMessageProducer.sendCreatedEventMessageToAllEventsTopic(eventDto);
        return eventDto;
    }

    public EventDto getEventById(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        return EventMapper.convertEventToEventDto(event);
    }

    @CacheEvict(cacheNames = {"allEvents","organizerEvents"}, allEntries = true)
    public EventDto updateEvent(UpdateEventRequest updateEventRequest, Long eventId, String token) {
        Event foundEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        String organiserUsername = foundEvent.getOrganizer().getUsername();
        String usernameExtractedFromToken = jwtService.extractUsername(token);

        if (!organiserUsername.equals(usernameExtractedFromToken)) {
            throw new NotYourEventException();
        }

        if (updateEventRequest.getEventName() != null && !updateEventRequest.getEventName().isEmpty()) {
            foundEvent.setEventName(updateEventRequest.getEventName());
        }

        if (updateEventRequest.getEventDescription() != null && !updateEventRequest.getEventDescription().isEmpty()) {
            foundEvent.setEventDescription(updateEventRequest.getEventDescription());
        }

        if (updateEventRequest.getLocation() != null && !updateEventRequest.getLocation().isEmpty()) {
            foundEvent.setLocation(updateEventRequest.getLocation());
        }

        if (updateEventRequest.getMaxAttendees() != null) {
            int maxAttendees = updateEventRequest.getMaxAttendees();
            int currentParticipants = foundEvent.getParticipants().size();

            if (maxAttendees < currentParticipants) {
                throw new IllegalStateException("New max attendees cannot be smaller than current amount of joined participants!");
            }

            foundEvent.setMaxAttendees(maxAttendees);

            if (foundEvent.getEventStatus() != EventStatus.FULL || maxAttendees > currentParticipants) {
                foundEvent.setEventStatus(maxAttendees == currentParticipants ? EventStatus.FULL : EventStatus.ACTIVE);
            }
        }

        if (updateEventRequest.getEventDate() != null && !updateEventRequest.getEventDate().isBefore(LocalDate.now())) {
            foundEvent.setEventDate(updateEventRequest.getEventDate());
        }

        if (updateEventRequest.getTicketPrice() != null) {
            double ticketPrice = updateEventRequest.getTicketPrice();
            if (ticketPrice < 0) {
                throw new IllegalArgumentException("Ticket price cannot be negative!");
            }
            foundEvent.setTicketPrice(ticketPrice);
        }

        if (updateEventRequest.getEventTarget() != null) {
            foundEvent.setEventTarget(updateEventRequest.getEventTarget());
        }

        foundEvent.setModifiedDate(LocalDateTime.now());
        eventRepository.save(foundEvent);

        return EventMapper.convertEventToEventDto(foundEvent);
    }

    @Cacheable(cacheNames = "organizerEvents")
    public List<EventDto> showAllOrganizerEvents(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
        List<Event> allByOrganizer = eventRepository.findAllByOrganizer(user);
        return EventMapper.convertListEventToListEventDto(allByOrganizer);

    }

    @Cacheable(cacheNames = "allEvents")
    public PageResponse<EventDto> showAllEvents(int page, int size) {
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
    @CacheEvict(value = {"allEvents","organizerEvents"}, allEntries = true)
    public void joinEvent(JoinEventRequest joinEventRequest, String eventName, String token) {
        String extractedToken = token.substring(7);

        String username = jwtService.extractUsername(extractedToken);
        User foundUserByToken = userRepository.findByUsername(username).orElseThrow(() -> UserNotFoundException.forUsername(username));
        if (!foundUserByToken.getEmail().equals(joinEventRequest.email)) {
            throw new IllegalArgumentException("You can use your email only!");
        }
        Event foundEvent = eventRepository.findByEventName(eventName).orElseThrow(() -> new EventNotFoundException(eventName));

        User user = userRepository.findByEmail(joinEventRequest.getEmail()).orElseThrow(() -> UserNotFoundException.forEmail(joinEventRequest.email));
        if (user.getId().equals(foundEvent.getOrganizer().getId())) {
            throw new IllegalArgumentException("You cannot join your own event!");
        }
        UserMapper.convertUserToUserDto(user);
        if (foundEvent.getEventStatus().equals(EventStatus.FULL)) {
            throw new IllegalArgumentException("You cannot join to the event because this is full.");
        } else if (foundEvent.getEventStatus().equals(EventStatus.CANCELLED)) {
            throw new IllegalArgumentException("You cannot join to the event because this event has been cancelled.");
        } else if (foundEvent.getEventStatus().equals(EventStatus.COMPLETED)) {
            throw new IllegalArgumentException("You can't join because this event has been finished.");
        } else if (foundEvent.getParticipants().contains(user)) {
            throw new IllegalArgumentException("You already joined to this event!");
        } else if (!isUserAdult(user.getBirthDate()) && foundEvent.getEventTarget().equals(EventTarget.ADULTS_ONLY)) {
            throw new IllegalArgumentException("You are too young to join this event!");
        }

        foundEvent.getParticipants().add(user);

        if (foundEvent.getParticipants().size() == foundEvent.getMaxAttendees()) {
            foundEvent.setEventStatus(EventStatus.FULL);
        }
        eventRepository.save(foundEvent);
    }

    @CacheEvict(cacheNames = {"allEvents","organizerEvents"}, allEntries = true)
    @Transactional
    @PreAuthorize("hasRole('USER')")
    public String deleteEvent(Long eventId, String token) {
        Event eventToDelete = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));

        String organiserUsername = eventToDelete.getOrganizer().getUsername();
        String usernameLoggedUser = jwtService.extractUsername(token);

        if (!usernameLoggedUser.equals(organiserUsername)) {
            throw new IllegalArgumentException("You can delete your events only!");
        } else if (eventToDelete.getEventStatus().equals(EventStatus.COMPLETED)
                || eventToDelete.getEventStatus().equals(EventStatus.CANCELLED)) {
            throw new IllegalArgumentException("You can delete ACTIVE or FULL events ONLY!");
        }

        eventToDelete.getParticipants().clear();
        eventToDelete.getReviews().clear();
        eventToDelete.getTickets().clear();

        eventRepository.saveAndFlush(eventToDelete);

        eventRepository.delete(eventToDelete);
        eventRepository.flush();

        EventDto eventDto = EventMapper.convertEventToEventDto(eventToDelete);
        eventDto.setEventStatus(EventStatus.CANCELLED);
        kafkaMessageProducer.sendCancelledEventMessageToCancellationTopic(eventDto);

        return eventToDelete.getEventName();
    }

    @CacheEvict(value = {"allEvents","organizerEvents"},allEntries = true)
    @Scheduled(cron = "0 32 13 * * ? ")
    public void updateEventsStatuses() {
        List<Event> activeEventsList = eventRepository.findAllByActiveEventStatus(EventStatus.ACTIVE);
        activeEventsList.forEach(event -> {
            if (event.getEventDate().isBefore(LocalDate.now())) {
                event.setEventStatus(EventStatus.COMPLETED);
            }
        });
        eventRepository.saveAll(activeEventsList);
        log.info("Event statuses have been updated");
    }

    boolean isUserAdult(LocalDate dateOfBirth) {
        return dateOfBirth.isBefore(LocalDate.now().minusYears(18));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    public EventDto getEventByName(String eventName) {
        Event event = eventRepository.findByEventName(eventName).orElseThrow(() -> new EventNotFoundException(eventName));
        return EventMapper.convertEventToEventDto(event);
    }
}
