package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring",uses = {UserMapperMapStruct.class})

public interface EventMapperMapStruct {


    @Mappings({
            @Mapping(target = "eventLocation",source = "location"),
            @Mapping(target = "organiser", source = "organizer")
    })
    EventDto convertEventToEventDto(Event event);

    @Mappings({
            @Mapping(target = "participants", ignore = true),
            @Mapping(target = "organizer", ignore = true),
            @Mapping(target = "modifiedDate", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "eventStatus", constant = "ACTIVE"),
            @Mapping(target = "createdDate", expression = "java(java.time.LocalDateTime.now())")
    })

    Event convertCreateEventRequestToEvent(CreateEventRequest createEventRequest);


    @Mappings({
            @Mapping(target = "eventLocation", source = "createEventRequest.location"),
            @Mapping(target = "createdDate", expression = "java(java.time.LocalDateTime.now())"),
            @Mapping(target = "eventStatus", constant = "ACTIVE"),
            @Mapping(target = "participants", expression = "java(java.util.Collections.emptyList())"),
            @Mapping(target = "organiser", source = "user")

    })
    EventDto createEventDtoFromRequest(CreateEventRequest createEventRequest, User user);

    List<EventDto> convertListEventToListEventDto(List<Event> eventList);
}
