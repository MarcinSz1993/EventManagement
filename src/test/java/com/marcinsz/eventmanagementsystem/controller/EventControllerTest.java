package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import com.marcinsz.eventmanagementsystem.request.JoinEventRequest;
import com.marcinsz.eventmanagementsystem.request.UpdateEventRequest;
import com.marcinsz.eventmanagementsystem.service.EventService;
import com.marcinsz.eventmanagementsystem.service.JwtService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class EventControllerTest {

    @Mock
    private EventService eventService;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private EventController eventController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void deleteEventShouldThrowIAEWithSpecifiedCommunicateWhenUserTriesToDeleteNotHisEvent() {
        Long eventId = 1L;
        String token = "token";

        Mockito.when(eventService.deleteEvent(eventId, token)).thenThrow(new IllegalArgumentException("You can delete your events only!"));

        ResponseEntity<String> response = eventController.deleteEvent(eventId, token);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertEquals("You can delete your events only!", response.getBody());

        Mockito.verify(eventService, Mockito.times(1)).deleteEvent(eventId, token);
    }

    @Test
    public void shouldDeleteEventSuccessfully() {
        Long eventId = 1L;
        String token = "token";
        String eventName = "Test event";
        Mockito.when(eventService.deleteEvent(eventId, token)).thenReturn(eventName);

        ResponseEntity<String> response = eventController.deleteEvent(eventId, token);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody()).isEqualTo("You deleted event Test event");

        Mockito.verify(eventService).deleteEvent(eventId, token);
    }

    @Test
    public void joinEventShouldHandleIEAWithSpecifiedCommunicateWhenUserTriesTypeNotHisEmail() {
        JoinEventRequest joinEventRequest = createTestJoinEventRequest();
        String eventName = "Example event";
        String token = "token";

        Mockito.doThrow(new IllegalArgumentException("You can use your email only!")).when(eventService).joinEvent(joinEventRequest, eventName, token);

        ResponseEntity<String> response = eventController.joinEvent(joinEventRequest, eventName, token);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody()).isEqualTo("You can use your email only!");

        Mockito.verify(eventService, Mockito.times(1)).joinEvent(joinEventRequest, eventName, token);

    }


    @Test
    public void joinEventShouldHandleIEAWithSpecifiedCommunicateWhenUserTriesToJoinCancelledEvent() {
        JoinEventRequest joinEventRequest = createTestJoinEventRequest();
        String eventName = "Example event";
        String token = "token";

        Mockito.doThrow(new IllegalArgumentException("You cannot join to the event because this event has been cancelled.")).when(eventService).joinEvent(joinEventRequest, eventName, token);

        ResponseEntity<String> response = eventController.joinEvent(joinEventRequest, eventName, token);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody()).isEqualTo("You cannot join to the event because this event has been cancelled.");

        Mockito.verify(eventService, Mockito.times(1)).joinEvent(joinEventRequest, eventName, token);

    }

    @Test
    public void joinEventShouldHandleIEAWithSpecifiedCommunicateWhenUserTriesToJoinFullEvent() {
        JoinEventRequest joinEventRequest = createTestJoinEventRequest();
        String eventName = "Example event";
        String token = "token";

        Mockito.doThrow(new IllegalArgumentException("Sorry, this event is full.")).when(eventService).joinEvent(joinEventRequest, eventName, token);

        ResponseEntity<String> response = eventController.joinEvent(joinEventRequest, eventName, token);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody()).isEqualTo("Sorry, this event is full.");

        Mockito.verify(eventService, Mockito.times(1)).joinEvent(joinEventRequest, eventName, token);

    }

    @Test
    public void joinEventShouldHandleIEAWithSpecifiedCommunicateWhenUserIsTooYoungToJoinTheEvent() {
        JoinEventRequest joinEventRequest = createTestJoinEventRequest();
        String eventName = "Example event";
        String token = "token";

        Mockito.doThrow(new IllegalArgumentException("You are too young to join this event!")).when(eventService).joinEvent(joinEventRequest, eventName, token);

        ResponseEntity<String> response = eventController.joinEvent(joinEventRequest, eventName, token);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody()).isEqualTo("You are too young to join this event!");

        Mockito.verify(eventService, Mockito.times(1)).joinEvent(joinEventRequest, eventName, token);

    }

    @Test
    public void joinEventShouldHandleIEAWithSpecifiedCommunicateWhenUserAlreadyJoinedTheEvent() {
        JoinEventRequest joinEventRequest = createTestJoinEventRequest();
        String eventName = "Example event";
        String token = "token";

        Mockito.doThrow(new IllegalArgumentException("You already joined to this event!")).when(eventService).joinEvent(joinEventRequest, eventName, token);

        ResponseEntity<String> response = eventController.joinEvent(joinEventRequest, eventName, token);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody()).isEqualTo("You already joined to this event!");

        Mockito.verify(eventService, Mockito.times(1)).joinEvent(joinEventRequest, eventName, token);
    }


    @Test
    public void shouldJoinEventSuccessfully() {
        JoinEventRequest joinEventRequest = createTestJoinEventRequest();
        String eventName = "Example event";
        String token = "token";

        Mockito.doNothing().when(eventService).joinEvent(joinEventRequest, eventName, token);

        ResponseEntity<String> response = eventController.joinEvent(joinEventRequest, eventName, token);

        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals("You joined to the event EXAMPLE EVENT.", response.getBody());

        Mockito.verify(eventService, Mockito.times(1)).joinEvent(joinEventRequest, eventName, token);
    }

    @Test
    public void showAllOrganizerEventsShouldReturnEmptyListAndParticularHeaderWhenUserDoesNotHaveAnyEvents() {
        String username = "username";

        Mockito.when(eventService.showAllOrganizerEvents(username)).thenReturn(Collections.emptyList());

        ResponseEntity<List<EventDto>> response = eventController.showAllOrganizerEvents(username);

        Assertions.assertThat(response.getBody()).isNotNull();
        assertEquals(Collections.emptyList(), response.getBody());
        Assertions.assertThat(response.getHeaders().containsKey("Message")).isTrue();
        assertEquals("User username does not have any events", response.getHeaders().getFirst("Message"));
        Mockito.verify(eventService, Mockito.times(1)).showAllOrganizerEvents(username);
    }

    @Test
    public void showAllOrganizerEventsShouldHandleUsernameNotFoundExceptionAndRespondsCode404WhenUsernameIsNotCorrect() {
        String notExistingUsername = "notExistingUsername";
        Mockito.when(eventService.showAllOrganizerEvents(notExistingUsername)).thenThrow(UserNotFoundException.forUsername(notExistingUsername));

        ResponseEntity<List<EventDto>> responseFromController = eventController.showAllOrganizerEvents(notExistingUsername);

        assertNotNull(responseFromController);
        Assertions.assertThat(responseFromController.getBody()).isNull();
        Assertions.assertThat(responseFromController.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));
    }

    @Test
    public void showAllOrganizerEventsSuccessfully() {
        User user = createTestUser();
        String username = user.getUsername();
        Event event1 = createTestEvent(user);
        Event event2 = createTestEvent(user);
        event2.setId(2L);
        event2.setEventName("Test Event 2");
        EventDto testEventDto1 = createTestEventDto(event1, user);
        EventDto testEventDto2 = createTestEventDto(event2, user);
        List<EventDto> expectedEventDtoList = new ArrayList<>();
        expectedEventDtoList.add(testEventDto1);
        expectedEventDtoList.add(testEventDto2);

        Mockito.when(eventService.showAllOrganizerEvents(username)).thenReturn(expectedEventDtoList);
        ResponseEntity<List<EventDto>> actualResponse = eventController.showAllOrganizerEvents(username);

        Assertions.assertThat(actualResponse.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(200));
        Assertions.assertThat(actualResponse.getBody()).isEqualTo(expectedEventDtoList);
        Assertions.assertThat(expectedEventDtoList).isEqualTo(actualResponse.getBody());
        Assertions.assertThat(Objects.requireNonNull(actualResponse.getBody()).size()).isEqualTo(2);

        Mockito.verify(eventService, Mockito.times(1)).showAllOrganizerEvents(username);

    }

    @Test
    public void shouldCreateEventSuccessfully() {
        CreateEventRequest createEventRequest = createTestEventRequest();
        User user = createTestUser();
        String token = "token";
        String username = user.getUsername();
        EventDto expectedEventDto = createTestExpectedEventDto(createEventRequest, user);

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(eventService.findByUsername(username)).thenReturn(user);
        Mockito.when(eventService.createEvent(createEventRequest, user)).thenReturn(expectedEventDto);

        ResponseEntity<EventDto> response = eventController.createEvent(createEventRequest, token);

        assertNotNull(response);
        assertEquals(expectedEventDto, response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        Mockito.verify(jwtService, Mockito.times(1)).extractUsername(token);
        Mockito.verify(eventService, Mockito.times(1)).findByUsername(username);
        Mockito.verify(eventService, Mockito.times(1)).createEvent(createEventRequest, user);
    }

    @Test
    public void updateEventShouldHandleEventNotFoundExceptionAndRespondsCode404WhenEventIdIsNotCorrect() {
        Long eventId = 999L;
        String token = "token";
        UpdateEventRequest updateEventRequest = createTestUpdateEventRequest();

        Mockito.when(eventService.updateEvent(updateEventRequest, eventId, token)).thenThrow(EventNotFoundException.class);
        assertThrows(EventNotFoundException.class,() -> eventController.updateEvent(updateEventRequest, eventId, token));
        Mockito.verify(jwtService, Mockito.never()).extractUsername(token);
    }


    @Test
    public void shouldUpdateEventSuccessfully() {
        User user = createTestUser();
        Event event = createTestEvent(user);
        Long eventId = event.getId();
        String token = "token";

        UpdateEventRequest updateEventRequest = createTestUpdateEventRequest();

        EventDto expectedEventDto = EventDto.builder()
                .eventName(updateEventRequest.getEventName())
                .eventDescription(updateEventRequest.getEventDescription())
                .eventLocation(updateEventRequest.getLocation())
                .maxAttendees(updateEventRequest.getMaxAttendees())
                .eventDate(updateEventRequest.getEventDate())
                .eventStatus(event.getEventStatus())
                .ticketPrice(updateEventRequest.getTicketPrice())
                .eventTarget(updateEventRequest.getEventTarget())
                .createdDate(event.getCreatedDate())
                .organiser(UserMapper.convertUserToOrganiserDto(user))
                .participants(Collections.emptyList())
                .build();

        Mockito.when(eventService.updateEvent(updateEventRequest, eventId, token)).thenReturn(expectedEventDto);
        ResponseEntity<EventDto> eventDtoResponseEntity = eventController.updateEvent(updateEventRequest, eventId, token);

        Assertions.assertThat(eventDtoResponseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(202));
        assertEquals(expectedEventDto, eventDtoResponseEntity.getBody());
        assertEquals(expectedEventDto.getEventDescription(), Objects.requireNonNull(Objects.requireNonNull(eventDtoResponseEntity.getBody()).getEventDescription()));
        Mockito.verify(eventService, Mockito.times(1)).updateEvent(updateEventRequest, eventId, token);
    }

    private EventDto createTestEventDto(Event event, User user) {
        return EventDto.builder()
                .eventName(event.getEventName())
                .eventDescription(event.getEventDescription())
                .eventLocation(event.getLocation())
                .maxAttendees(event.getMaxAttendees())
                .eventDate(event.getEventDate())
                .eventStatus(event.getEventStatus())
                .ticketPrice(event.getTicketPrice())
                .eventTarget(event.getEventTarget())
                .createdDate(event.getCreatedDate())
                .organiser(UserMapper.convertUserToOrganiserDto(user))
                .participants(Collections.emptyList())
                .build();
    }


    private EventDto createTestExpectedEventDto(CreateEventRequest createEventRequest, User user) {
        return EventDto.builder()
                .id(1L)
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

    private CreateEventRequest createTestEventRequest() {
        return CreateEventRequest
                .builder()
                .eventName("Test Event")
                .eventDescription("Test Description")
                .location("Test Location")
                .maxAttendees(10)
                .eventDate(null)
                .ticketPrice(20.0)
                .eventTarget(EventTarget.EVERYBODY)
                .build();
    }

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("encodedPassword")
                .birthDate(LocalDate.of(1993, 4, 19))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .build();
    }

    private Event createTestEvent(User user) {
        return Event.builder()
                .id(1L)
                .eventName("Test Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100.0)
                .eventTarget(EventTarget.EVERYBODY)
                .createdDate(LocalDateTime.of(2024, 1, 6, 10, 0))
                .modifiedDate(null)
                .participants(new ArrayList<>())
                .organizer(user)
                .build();
    }

    private UpdateEventRequest createTestUpdateEventRequest() {
        return UpdateEventRequest.builder()
                .eventName("Updated event name")
                .eventDescription("Updated event description")
                .location("Updated location")
                .maxAttendees(20)
                .eventDate(LocalDate.of(2024, 12, 31))
                .ticketPrice(20.0)
                .eventTarget(EventTarget.EVERYBODY)
                .build();
    }

    private JoinEventRequest createTestJoinEventRequest() {
        return JoinEventRequest.builder()
                .email("example@example.com")
                .build();
    }
}