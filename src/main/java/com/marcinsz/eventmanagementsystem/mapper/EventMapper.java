package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
                event.getEventType(),
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
                createEventRequest.getEventType(),
                LocalDateTime.now(),
                null,
                null,
                null);
    }

    public static List<EventDto> convertListEventToListEventDto(List<Event> eventList){
       return eventList.stream()
                .map(EventMapper::convertEventToEventDto).collect(Collectors.toList());
    }
}
