package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.dto.OrganiserDto;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class EventMapperMapStructTest {

    @Autowired
    private EventMapperMapStruct eventMapperMapStruct;

    @Test
    public void shouldConvertEventToEventDtoSuccessfully(){

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
                .eventType(EventType.CHILDREN)
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

        EventDto eventDto = eventMapperMapStruct.convertEventToEventDto(event);

        assertEquals(event.getEventName(),eventDto.getEventName());
        assertEquals(event.getEventDescription(), eventDto.getEventDescription());
        assertEquals(event.getMaxAttendees(), eventDto.getMaxAttendees());
        assertEquals(event.getEventDate(), eventDto.getEventDate());
        assertEquals(event.getEventStatus(), eventDto.getEventStatus());
        assertEquals(event.getTicketPrice(), eventDto.getTicketPrice());
        assertEquals(event.getEventType(), eventDto.getEventType());
        assertEquals(event.getCreatedDate(), eventDto.getCreatedDate());
        assertEquals(organiserDto, eventDto.getOrganiser());
        assertTrue(eventDto.getParticipants().isEmpty());
    }

        @Test
        public void shouldConvertCreateEventRequestToEventSuccessfully(){
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
            CreateEventRequest createEventRequest = CreateEventRequest.builder()
                    .eventName("Test Event")
                    .eventDescription("Test description")
                    .location("Lublin")
                    .maxAttendees(10)
                    .eventDate(LocalDate.of(2024, 6, 20))
                    .ticketPrice(100)
                    .eventType(EventType.EVERYBODY)
                    .build();

            Event expectedEvent = Event.builder()
                    .id(1L)
                    .eventName("Test Event")
                    .eventDescription("Test description")
                    .location("Lublin")
                    .maxAttendees(10)
                    .eventDate(LocalDate.of(2024, 6, 20))
                    .eventStatus(EventStatus.ACTIVE)
                    .ticketPrice(20.0)
                    .eventType(EventType.EVERYBODY)
                    .createdDate(LocalDateTime.now())
                    .modifiedDate(null)
                    .participants(Collections.emptyList())
                    .organizer(user)
                    .build();

            Event acutalEvent = eventMapperMapStruct.convertCreateEventRequestToEvent(createEventRequest);

            assertEquals(expectedEvent.getEventName(),acutalEvent.getEventName());
            assertEquals(expectedEvent.getEventDescription(),acutalEvent.getEventDescription());
            assertEquals(expectedEvent.getLocation(),acutalEvent.getLocation());
            assertEquals(expectedEvent.getMaxAttendees(),acutalEvent.getMaxAttendees());
            assertEquals(expectedEvent.getEventDate(),acutalEvent.getEventDate());
            assertEquals(expectedEvent.getEventStatus(),acutalEvent.getEventStatus());


        }
}