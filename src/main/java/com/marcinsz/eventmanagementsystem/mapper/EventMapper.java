package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventMapper {

    private final UserMapper userMapper;

    public EventDto convertEventToEventDto(Event event) {
        return new EventDto(
                event.getEventName(),
                event.getEventDescription(),
                event.getLocation(),
                event.getMaxAttendees(),
                event.getEventDate(),
                event.getEventStatus(),
                event.getTicketPrice(),
                event.getEventType(),
                event.getCreatedDate(),
                userMapper.convertUserToOrganiserDto(event.getOrganizer()),
                userMapper.convertListUserToListUserDto(event.getParticipants())
        );
    }

    public Event convertCreateEventRequestToEvent(CreateEventRequest createEventRequest) {
        return new Event(
                createEventRequest.getEventName(),
                createEventRequest.getEventDescription(),
                createEventRequest.getLocation(),
                createEventRequest.getMaxAttendees(),
                createEventRequest.getEventDate(),
                EventStatus.ACTIVE,
                createEventRequest.getTicketPrice(),
                createEventRequest.getEventType(),
                LocalDateTime.now(),
                null,
                null,
                null
        );
    }

    public EventDto createEventDtoFromRequest(CreateEventRequest createEventRequest, User user) {
        return new EventDto(
                createEventRequest.getEventName(),
                createEventRequest.getEventDescription(),
                createEventRequest.getLocation(),
                createEventRequest.getMaxAttendees(),
                createEventRequest.getEventDate(),
                EventStatus.ACTIVE,
                createEventRequest.getTicketPrice(),
                createEventRequest.getEventType(),
                LocalDateTime.now(),
                userMapper.convertUserToOrganiserDto(user),
                Collections.emptyList()
        );
    }

    public List<EventDto> convertListEventToListEventDto(List<Event> eventList) {
        return eventList.stream()
                .map(this::convertEventToEventDto)
                .collect(Collectors.toList());
    }
}