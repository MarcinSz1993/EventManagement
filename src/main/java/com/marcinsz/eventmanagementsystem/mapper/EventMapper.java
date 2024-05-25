package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import lombok.*;

import java.time.LocalDateTime;

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
                event.getOrganizer()
        );
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
}
