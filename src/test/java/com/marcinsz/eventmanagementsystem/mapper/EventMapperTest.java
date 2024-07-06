package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.dto.OrganiserDto;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EventMapperTest {
    @Test
    public void shouldMapEventToEventDto() {
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993, 4, 20))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

        Event event = Event.builder()
                .id(1L)
                .eventName("Test Event")
                .eventDescription("Test description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(20.0)
                .eventTarget(EventTarget.CHILDREN)
                .createdDate(LocalDateTime.now())
                .modifiedDate(null)
                .participants(Collections.emptyList())
                .organizer(user)
                .build();

        OrganiserDto organiserDto = OrganiserDto.builder()
                .firstName("John")
                .lastName("Smith")
                .userName("johnny")
                .email("john@smith.com")
                .phoneNumber("123456789")
                .build();

        EventDto eventDto = EventMapper.convertEventToEventDto(event);

        assertEquals(event.getEventName(), eventDto.getEventName());
        assertEquals(event.getEventDescription(), eventDto.getEventDescription());
        assertEquals(event.getMaxAttendees(), eventDto.getMaxAttendees());
        assertEquals(event.getEventDate(), eventDto.getEventDate());
        assertEquals(event.getEventStatus(), eventDto.getEventStatus());
        assertEquals(event.getTicketPrice(), eventDto.getTicketPrice());
        assertEquals(event.getEventTarget(), eventDto.getEventTarget());
        assertEquals(event.getCreatedDate(), eventDto.getCreatedDate());
        assertEquals(organiserDto, eventDto.getOrganiser());
        assertTrue(eventDto.getParticipants().isEmpty());
    }
    @Test
    public void shouldMapCreateEventRequestToEvent() {
        CreateEventRequest createEventRequest = CreateEventRequest.builder()
                .eventName("Example Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .ticketPrice(100)
                .eventTarget(EventTarget.EVERYBODY)
                .build();

        Event event = EventMapper.convertCreateEventRequestToEvent(createEventRequest);

        assertEquals(createEventRequest.getEventName(), event.getEventName());
        assertEquals(createEventRequest.getEventDescription(), event.getEventDescription());
        assertEquals(createEventRequest.getLocation(), event.getLocation());
        assertEquals(createEventRequest.getMaxAttendees(), event.getMaxAttendees());
        assertEquals(createEventRequest.getEventDate(), event.getEventDate());
        assertEquals(EventStatus.ACTIVE, event.getEventStatus());
        assertEquals(createEventRequest.getTicketPrice(), event.getTicketPrice());
        assertEquals(createEventRequest.getEventTarget(), event.getEventTarget());
        assertNull(event.getModifiedDate());
        assertNull(event.getParticipants());
        assertNull(event.getOrganizer());
    }
    @Test
    public void shouldMapListEventToListEventDto() {
        List<Event> eventList = new ArrayList<>();
        User user1 = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993, 4, 20))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

        User user2 = User.builder()
                .id(2L)
                .firstName("Kate")
                .lastName("Anderson")
                .email("kate@anderson.com")
                .username("katty")
                .password("qwerty")
                .birthDate(LocalDate.of(1996, 1, 10))
                .role(Role.USER)
                .phoneNumber("987654321")
                .accountNumber("0987654321")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

        Event event1 = Event.builder()
                .id(1L)
                .eventName("Test Event")
                .eventDescription("Test description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(20.0)
                .eventTarget(EventTarget.CHILDREN)
                .createdDate(LocalDateTime.now())
                .modifiedDate(null)
                .participants(Collections.emptyList())
                .organizer(user1)
                .build();

        Event event2 = Event.builder()
                .id(2L)
                .eventName("Test Event")
                .eventDescription("Test description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(20.0)
                .eventTarget(EventTarget.CHILDREN)
                .createdDate(LocalDateTime.now())
                .modifiedDate(null)
                .participants(Collections.emptyList())
                .organizer(user2)
                .build();
        eventList.add(event1);
        eventList.add(event2);

        List<EventDto> listEventDto = EventMapper.convertListEventToListEventDto(eventList);

        assertEquals(eventList.size(),listEventDto.size());
        assertEquals(eventList.get(0).getEventName(),listEventDto.get(0).getEventName());
        assertEquals(eventList.get(0).getEventDescription(),listEventDto.get(0).getEventDescription());
        assertEquals(eventList.get(0).getLocation(),listEventDto.get(0).getEventLocation());
        assertEquals(eventList.get(0).getMaxAttendees(),listEventDto.get(0).getMaxAttendees());
        assertEquals(eventList.get(0).getEventDate(),listEventDto.get(0).getEventDate());
        assertEquals(eventList.get(0).getEventStatus(),listEventDto.get(0).getEventStatus());
        assertEquals(eventList.get(0).getTicketPrice(),listEventDto.get(0).getTicketPrice());
        assertEquals(eventList.get(0).getEventTarget(),listEventDto.get(0).getEventTarget());
        assertEquals(eventList.get(0).getCreatedDate(),listEventDto.get(0).getCreatedDate());
        assertEquals(UserMapper.convertUserToOrganiserDto(user1),listEventDto.get(0).getOrganiser());
        assertTrue(listEventDto.get(0).getParticipants().isEmpty());
    }
}