package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class EventMapper {

    public static EventDto convertEventToEventDto(Event event){
        return new EventDto(
                event.getEventName(),
                event.getEventDescription(),
                event.getLocation(),
                event.getMaxAttendees(),
                event.getEventDate(),
                event.getEventStatus(),
                event.getTicketPrice(),
                event.getEventTarget(),
                event.getCreatedDate(),
                UserMapper.convertUserToOrganiserDto(event.getOrganizer()),
                UserMapper.convertListUserToListUserDto(event.getParticipants()));
    }

    public static Event convertCreateEventRequestToEvent(CreateEventRequest createEventRequest){
        return new Event(createEventRequest.getEventName(),
                createEventRequest.getEventDescription(),
                createEventRequest.getLocation(),
                createEventRequest.getMaxAttendees(),
                createEventRequest.getEventDate(),
                EventStatus.ACTIVE,
                createEventRequest.getTicketPrice(),
                createEventRequest.getEventTarget(),
                LocalDateTime.now(),
                null,
                null,
                null);
    }

    public static EventDto convertCreateEventRequestToEventDto(CreateEventRequest createEventRequest, User user){
        return EventDto.builder()
                .eventName(createEventRequest.getEventName())
                .eventDescription(createEventRequest.getEventDescription())
                .eventLocation(createEventRequest.getLocation())
                .maxAttendees(createEventRequest.getMaxAttendees())
                .eventDate(createEventRequest.getEventDate())
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(createEventRequest.getTicketPrice())
                .eventTarget(createEventRequest.getEventTarget())
                .createdDate(LocalDateTime.now())
                .organiser(UserMapper.convertUserToOrganiserDto(user))
                .participants(Collections.emptyList())
                .build();
    }
    public static List<EventDto> convertListEventToListEventDto(List<Event> eventList){
       return eventList.stream()
                .map(EventMapper::convertEventToEventDto).collect(Collectors.toList());
    }
}
