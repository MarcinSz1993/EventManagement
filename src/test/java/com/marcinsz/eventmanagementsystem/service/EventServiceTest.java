package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.dto.OrganiserDto;
import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.exception.EventNotFoundException;
import com.marcinsz.eventmanagementsystem.exception.NotYourEventException;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import com.marcinsz.eventmanagementsystem.request.JoinEventRequest;
import com.marcinsz.eventmanagementsystem.request.UpdateEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceTest {

    @InjectMocks
    private EventService eventService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private KafkaMessageProducer kafkaMessageProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldCreateEventSuccessfullyWhenGivenValidInput() {
        CreateEventRequest createEventRequest = CreateEventRequest.builder()
                .eventName("Example Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .ticketPrice(100)
                .eventTarget(EventTarget.EVERYBODY)
                .build();

        User user = createTestUser();
        Event event = createTestEvent(user);
        OrganiserDto organiserDto = createTestOrganiserDto(user);

        EventDto expectedEventDto = EventDto.builder()
                .eventName("Example Event")
                .eventDescription("Example description")
                .eventLocation("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100)
                .createdDate(event.getCreatedDate())
                .organiser(organiserDto)
                .participants(Collections.emptyList())
                .build();

        try (MockedStatic<EventMapper> eventMapperMockedStatic = Mockito.mockStatic(EventMapper.class)) {
            eventMapperMockedStatic.when(() -> EventMapper.convertCreateEventRequestToEvent(createEventRequest)).thenReturn(event);
            eventMapperMockedStatic.when(() -> EventMapper.convertEventToEventDto(event)).thenReturn(expectedEventDto);
        }
        event.setOrganizer(user);
        when(eventRepository.save(event)).thenReturn(event);
        doNothing().when(kafkaMessageProducer).sendCreatedEventMessageToAllEventsTopic(expectedEventDto);

        EventDto acutalEventDto = eventService.createEvent(createEventRequest, user);

        assertEquals(expectedEventDto.getEventName(), acutalEventDto.getEventName());
        assertEquals(expectedEventDto.getEventDescription(), acutalEventDto.getEventDescription());
        assertEquals(expectedEventDto.getEventLocation(), acutalEventDto.getEventLocation());
        assertEquals(expectedEventDto.getMaxAttendees(), acutalEventDto.getMaxAttendees());
        assertEquals(expectedEventDto.getEventDate(), acutalEventDto.getEventDate());
        assertEquals(expectedEventDto.getEventStatus(), acutalEventDto.getEventStatus());
        assertEquals(expectedEventDto.getTicketPrice(), acutalEventDto.getTicketPrice());
        assertEquals(expectedEventDto.getOrganiser(), acutalEventDto.getOrganiser());
        assertEquals(expectedEventDto.getParticipants(), acutalEventDto.getParticipants());

    }

    @Test
    public void shouldUpdateEventSuccessfullyWhenGivenValidInput() {
        UpdateEventRequest updateEventRequest = UpdateEventRequest.builder()
                .eventName("Updated name")
                .eventDescription("Updated description")
                .location("Warszawa")
                .maxAttendees(5)
                .eventDate(LocalDate.of(2024, 7, 1))
                .ticketPrice(10.0)
                .eventTarget(EventTarget.SINGLES)
                .build();

        User user = createTestUser();
        Event event = createTestEvent(user);
        OrganiserDto organiserDto = createTestOrganiserDto(user);

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
                .organiser(organiserDto)
                .participants(Collections.emptyList())
                .build();
        Long eventIdToUpdate = 2L;
        String token = "token";

        when(eventRepository.findById(eventIdToUpdate)).thenReturn(Optional.of(event));
        when(jwtService.extractUsername(token)).thenReturn("johnny");
        event.setModifiedDate(LocalDateTime.now());
        when(eventRepository.save(event)).thenReturn(event);

        try (MockedStatic<EventMapper> eventMapperMockedStatic = mockStatic(EventMapper.class)) {

            eventMapperMockedStatic.when(() -> EventMapper.convertEventToEventDto(event)).thenReturn(expectedEventDto);
        }
        EventDto actualEventDto = eventService.updateEvent(updateEventRequest, eventIdToUpdate, token);

        assertEquals(updateEventRequest.getEventName(), actualEventDto.getEventName());

    }

    @Test
    public void shouldSuccessfullyShowAllOrganiserEvents() {
        List<EventDto> expectedEventDtoList = new ArrayList<>();
        List<Event> eventList = new ArrayList<>();

        User user = createTestUser();
        Event event1 = Event.builder()
                .id(1L)
                .eventName("Example Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100.0)
                .eventTarget(EventTarget.EVERYBODY)
                .createdDate(LocalDateTime.of(2024, 1, 6, 10, 0))
                .modifiedDate(null)
                .participants(Collections.emptyList())
                .organizer(user)
                .build();

        Event event2 = Event.builder()
                .id(2L)
                .eventName("Example Event2")
                .eventDescription("Example description2")
                .location("Lublin2")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2025, 7, 21))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(10.0)
                .eventTarget(EventTarget.EVERYBODY)
                .createdDate(LocalDateTime.of(2024, 2, 5, 10, 0))
                .modifiedDate(null)
                .participants(Collections.emptyList())
                .organizer(user)
                .build();

        OrganiserDto organiserDto = createTestOrganiserDto(user);

        EventDto eventDto1 = EventDto.builder()
                .eventName("Example Event")
                .eventDescription("Example description")
                .eventLocation("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100)
                .eventTarget(EventTarget.EVERYBODY)
                .createdDate(LocalDateTime.of(2024, 1, 6, 10, 0))
                .organiser(organiserDto)
                .participants(Collections.emptyList())
                .build();

        EventDto eventDto2 = EventDto.builder()
                .eventName("Example Event2")
                .eventDescription("Example description2")
                .eventLocation("Lublin2")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 7, 21))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100)
                .eventTarget(EventTarget.EVERYBODY)
                .createdDate(LocalDateTime.of(2024, 2, 5, 10, 0))
                .organiser(organiserDto)
                .participants(Collections.emptyList())
                .build();

        String username = "johnny";

        expectedEventDtoList.add(eventDto1);
        expectedEventDtoList.add(eventDto2);

        eventList.add(event1);
        eventList.add(event2);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(eventRepository.findAllByOrganizer(user)).thenReturn(eventList);

        try (MockedStatic<EventMapper> eventMapperMockedStatic = mockStatic(EventMapper.class)) {
            eventMapperMockedStatic.when(() -> EventMapper.convertListEventToListEventDto(eventList)).thenReturn(eventList);
        }

        List<EventDto> actualEventDtoList = eventService.showAllOrganizerEvents(username);
        assertEquals(expectedEventDtoList.size(), actualEventDtoList.size());
        assertEquals(expectedEventDtoList.getFirst().getEventName(), actualEventDtoList.getFirst().getEventName());
        assertEquals(expectedEventDtoList.getFirst().getEventDescription(), actualEventDtoList.getFirst().getEventDescription());
        assertEquals(expectedEventDtoList.getFirst().getEventLocation(), actualEventDtoList.getFirst().getEventLocation());
        assertEquals(expectedEventDtoList.getFirst().getMaxAttendees(), actualEventDtoList.getFirst().getMaxAttendees());
        assertEquals(expectedEventDtoList.getFirst().getEventDate(), actualEventDtoList.getFirst().getEventDate());
        assertEquals(expectedEventDtoList.getFirst().getEventStatus(), actualEventDtoList.getFirst().getEventStatus());
        assertEquals(expectedEventDtoList.getFirst().getTicketPrice(), actualEventDtoList.getFirst().getTicketPrice());
        assertEquals(expectedEventDtoList.getFirst().getEventTarget(), actualEventDtoList.getFirst().getEventTarget());
        assertEquals(expectedEventDtoList.getFirst().getCreatedDate(), actualEventDtoList.getFirst().getCreatedDate());
        assertEquals(expectedEventDtoList.getFirst().getOrganiser(), actualEventDtoList.getFirst().getOrganiser());
        assertEquals(expectedEventDtoList.getFirst().getParticipants(), actualEventDtoList.getFirst().getParticipants());
    }

    @Test
    public void shouldSuccessfullyJoinEventWhenInputIsValid() {
        JoinEventRequest joinEventRequest = JoinEventRequest.builder()
                .email("john@smith.com")
                .build();
        String eventName = "Test Event";
        String token = "token";

        User user = createTestUser();
        UserDto userDto = UserDto.builder()
                .userId(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .birthDate(LocalDate.of(1993, 4, 19))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .build();

        Event event = createTestEvent(user);

        when(jwtService.extractUsername(token)).thenReturn("johnny");
        when(userRepository.findByUsername("johnny")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventName(eventName)).thenReturn(Optional.of(event));
        when(userRepository.findByEmail(joinEventRequest.getEmail())).thenReturn(Optional.of(user));

        try (MockedStatic<UserMapper> userMapperMockedStatic = mockStatic(UserMapper.class)) {
            userMapperMockedStatic.when(() -> UserMapper.convertUserToUserDto(user)).thenReturn(userDto);
        }

        eventService.joinEvent(joinEventRequest, eventName, token);

        assertTrue(event.getParticipants().contains(user));
        verify(eventRepository).save(event);
    }

    @Test
    public void joinEventShouldThrowIAEWithSpecifiedInformationWhenUserTypesNotHisEmail(){
        User user = createTestUser();
        String username = user.getUsername();
        String token = "token";
        String eventName = "Test event";
        JoinEventRequest joinEventRequest = JoinEventRequest.builder()
                .email("otherUserEmail@email.com")
                .build();

        Mockito.when(jwtService.extractUsername(token)).thenReturn(user.getUsername());
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> eventService.joinEvent(joinEventRequest, eventName, token));
        assertEquals("You can use your email only!",illegalArgumentException.getMessage());

        Mockito.verify(jwtService,Mockito.times(1)).extractUsername(token);
        Mockito.verify(userRepository,Mockito.times(1)).findByUsername(username);
        Mockito.verify(eventRepository,Mockito.never()).save(Mockito.any(Event.class));

    }

    @Test
    public void joinEventShouldThrowIAEWithSpecifiedInformationWhenUserAlreadyJoinedTheEvent(){
        User user = createTestUser();
        Event event = createTestEvent(user);
        event.setParticipants(List.of(user));
        String username = user.getUsername();
        String token = "token";
        String eventName = "Test event";
        JoinEventRequest joinEventRequest = JoinEventRequest.builder()
                .email(user.getEmail())
                .build();

        Mockito.when(jwtService.extractUsername(token)).thenReturn(user.getUsername());
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findByEventName(eventName)).thenReturn(Optional.of(event));
        Mockito.when(userRepository.findByEmail(joinEventRequest.getEmail())).thenReturn(Optional.of(user));

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> eventService.joinEvent(joinEventRequest, eventName, token));
        assertEquals("You already joined to this event!",illegalArgumentException.getMessage());

        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(userRepository).findByUsername(username);
        Mockito.verify(eventRepository).findByEventName(eventName);
        Mockito.verify(userRepository).findByEmail(joinEventRequest.getEmail());
        Mockito.verify(eventRepository,Mockito.never()).save(Mockito.any(Event.class));
    }

    @Test
    public void joinEventShouldThrowIAEWithSpecifiedInformationWhenUserIsTooYoungToJoinEventsForAdults(){
        User user = createTestUser();
        User tooYoungUser = User.builder()
                .id(2L)
                .firstName("Tony")
                .lastName("Smith")
                .email("tony@smith.com")
                .username("tony@smith.com")
                .password("123456")
                .birthDate(LocalDate.of(2016, 4, 19))
                .role(Role.USER)
                .phoneNumber("111111111")
                .accountNumber("2222222222")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .reviews(Collections.emptyList())
                .build();
        tooYoungUser.setBirthDate(LocalDate.of(2015,10,11));
        JoinEventRequest joinEventRequest = JoinEventRequest.builder()
                .email(tooYoungUser.getEmail())
                .build();
        String username = tooYoungUser.getUsername();
        String token = "token";
        String eventName = "Test Event";
        Event event = createTestEvent(user);
        event.setEventTarget(EventTarget.ADULTS_ONLY);

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(tooYoungUser));
        Mockito.when(eventRepository.findByEventName(eventName)).thenReturn(Optional.of(event));
        Mockito.when(userRepository.findByEmail(joinEventRequest.getEmail())).thenReturn(Optional.of(tooYoungUser));

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> eventService.joinEvent(joinEventRequest, eventName, token));
        assertEquals("You are too young to join this event!",illegalArgumentException.getMessage());

        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(userRepository).findByUsername(username);
        Mockito.verify(eventRepository).findByEventName(eventName);
        Mockito.verify(userRepository).findByEmail(joinEventRequest.getEmail());
        Mockito.verify(eventRepository,Mockito.never()).save(Mockito.any(Event.class));
    }

    @Test
    public void joinEventShouldThrowIAEWithSpecifiedInformationWhenUserTriesToJoinFullEvent(){
        User user = createTestUser();
        String eventName = "Test Event";
        Event event = createTestEvent(user);
        event.setEventStatus(EventStatus.COMPLETED);
        String token = "token";
        String username = user.getUsername();

        JoinEventRequest joinEventRequest = JoinEventRequest.builder()
                .email(user.getEmail())
                .build();

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findByEventName(eventName)).thenReturn(Optional.of(event));
        Mockito.when(userRepository.findByEmail(joinEventRequest.getEmail())).thenReturn(Optional.of(user));

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> eventService.joinEvent(joinEventRequest, eventName, token));
        assertEquals("Sorry, this event is full.",illegalArgumentException.getMessage());

        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(userRepository).findByUsername(username);
        Mockito.verify(eventRepository).findByEventName(eventName);
        Mockito.verify(userRepository).findByEmail(joinEventRequest.getEmail());
        Mockito.verify(eventRepository,Mockito.never()).save(event);
    }

    @Test
    public void joinEventShouldThrowIAEWithSpecifiedInformationWhenUserTriesToJoinCancelledEvent(){
        User user = createTestUser();
        String eventName = "Test Event";
        Event event = createTestEvent(user);
        event.setEventStatus(EventStatus.CANCELLED);
        String token = "token";
        String username = user.getUsername();

        JoinEventRequest joinEventRequest = JoinEventRequest.builder()
                .email(user.getEmail())
                .build();

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findByEventName(eventName)).thenReturn(Optional.of(event));
        Mockito.when(userRepository.findByEmail(joinEventRequest.getEmail())).thenReturn(Optional.of(user));

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> eventService.joinEvent(joinEventRequest, eventName, token));
        assertEquals("You cannot join to the event because this event has been cancelled.",illegalArgumentException.getMessage());

        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(userRepository).findByUsername(username);
        Mockito.verify(eventRepository).findByEventName(eventName);
        Mockito.verify(userRepository).findByEmail(joinEventRequest.getEmail());
        Mockito.verify(eventRepository,Mockito.never()).save(event);
    }

    @Test
    public void shouldSuccessfullyDeleteEvent() {
        Long eventId = 1L;
        String token = "token";
        User user = createTestUser();
        Event event = createTestEvent(user);
        String expectedName = event.getEventName();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(jwtService.extractUsername(token)).thenReturn("johnny");

        String actualName = eventService.deleteEvent(eventId, token);

        assertEquals(expectedName, actualName);
        verify(eventRepository).findById(eventId);
        verify(eventRepository).deleteById(eventId);
    }

    @Test
    public void deleteEventShouldThrowIAEWithSpecifiedInformationWhenUserTriesToDeleteNotHisEvent(){
        Long eventId = 1L;
        String token = "token";
        User user = createTestUser();
        Event event = createTestEvent(user);
        String loggedUsername = "loggedUser";

        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        Mockito.when(jwtService.extractUsername(token)).thenReturn(loggedUsername);

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> eventService.deleteEvent(eventId, token));
        assertEquals("You can delete your events only!",illegalArgumentException.getMessage());

        Mockito.verify(eventRepository).findById(eventId);
        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(eventRepository,Mockito.never()).delete(event);
        Mockito.verify(kafkaMessageProducer,Mockito.never()).sendCancelledEventMessageToCancellationTopic(EventMapper.convertEventToEventDto(event));
    }

    @Test
    public void isUserAdultShouldReturnTrueWhenAgeIsMoreOrEquals18() {
        LocalDate dateOfBirth = LocalDate.of(2000, 1, 1);
        assertTrue(eventService.isUserAdult(dateOfBirth));
    }

    @Test
    public void isUserAdultShouldReturnFalseWhenAgeIsLessThan18() {
        LocalDate dateOfBirth = LocalDate.of(2010, 1, 1);
        assertFalse(eventService.isUserAdult(dateOfBirth));
    }

    @Test
    public void shouldCorrectlyUpdateAllUpdateableFields() {
        Long eventId = 1L;
        String token = "token";
        String usernameFromToken = "johnny";
        ArrayList<UserDto> participants = new ArrayList<>();
        User user = createTestUser();
        Event event = createTestEvent(user);
        UpdateEventRequest updateEventRequest = UpdateEventRequest.builder()
                .eventName("Updated event name")
                .eventDescription("Updated event description")
                .location("Updated location")
                .maxAttendees(20)
                .eventDate(LocalDate.of(2024, 12, 31))
                .ticketPrice(20.0)
                .eventTarget(EventTarget.EVERYBODY)
                .build();
        OrganiserDto organiserDto = createTestOrganiserDto(user);

        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.ofNullable(event));
        Mockito.when(jwtService.extractUsername(token)).thenReturn(usernameFromToken);
        try (MockedStatic<UserMapper> userMapperMockedStatic = mockStatic(UserMapper.class)) {
            userMapperMockedStatic.when(() -> UserMapper.convertUserToOrganiserDto(user)).thenReturn(organiserDto);
            userMapperMockedStatic.when(() -> {
                assert event != null;
                UserMapper.convertListUserToListUserDto(event.getParticipants());
            }).thenReturn(participants);
        }

        EventDto actualEventDto = eventService.updateEvent(updateEventRequest, eventId, token);

            assert event != null;
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
                    .organiser(organiserDto)
                    .participants(participants)
                    .build();

            assertEquals(expectedEventDto.getEventName(), actualEventDto.getEventName());
            assertEquals(expectedEventDto.getEventDescription(), actualEventDto.getEventDescription());
            assertEquals(expectedEventDto.getEventLocation(), actualEventDto.getEventLocation());
            assertEquals(expectedEventDto.getMaxAttendees(), actualEventDto.getMaxAttendees());
            assertEquals(expectedEventDto.getEventDate(), actualEventDto.getEventDate());
            assertEquals(expectedEventDto.getTicketPrice(), actualEventDto.getTicketPrice());
            assertEquals(expectedEventDto.getEventTarget(), actualEventDto.getEventTarget());

            Mockito.verify(eventRepository).findById(eventId);
            Mockito.verify(jwtService).extractUsername(token);
            Mockito.verify(eventRepository).save(event);
    }

    @Test
    public void shouldCorrectlyUpdateOneUpdateableField() {
        Long eventId = 1L;
        User user = createTestUser();
        Event event = createTestEvent(user);
        String token = "token";
        OrganiserDto organiserDto = createTestOrganiserDto(user);
        ArrayList<UserDto> participants = new ArrayList<>();

        UpdateEventRequest updateEventRequest = UpdateEventRequest.builder()
                .eventDescription("Updated event description")
                .build();

        EventDto expectedEventDto = EventDto.builder()
                .eventName(event.getEventName())
                .eventDescription(updateEventRequest.getEventDescription())
                .eventLocation(event.getLocation())
                .maxAttendees(event.getMaxAttendees())
                .eventDate(event.getEventDate())
                .eventStatus(event.getEventStatus())
                .ticketPrice(event.getTicketPrice())
                .eventTarget(event.getEventTarget())
                .createdDate(event.getCreatedDate())
                .organiser(organiserDto)
                .participants(participants)
                .build();

        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        Mockito.when(jwtService.extractUsername(token)).thenReturn(user.getUsername());

        try (MockedStatic<UserMapper> userMapperMockedStatic = mockStatic(UserMapper.class)) {
            userMapperMockedStatic.when(() -> UserMapper.convertUserToOrganiserDto(user)).thenReturn(organiserDto);
            userMapperMockedStatic.when(() -> UserMapper.convertListUserToListUserDto(event.getParticipants())).thenReturn(participants);
        }
        EventDto actualEventDto = eventService.updateEvent(updateEventRequest, eventId, token);

        assertEquals(expectedEventDto.getEventDescription(), actualEventDto.getEventDescription());

        Mockito.verify(eventRepository).findById(eventId);
        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(eventRepository).save(event);
    }

    @Test
    public void updateEventShouldThrowNotYourEventExceptionWhenOrganiserUsernameAndUsernameFromTokenNotMatch(){
        Long eventId = 1L;
        User user = createTestUser();
        Event event = createTestEvent(user);
        String token = "token";
        String usernameFromToken = "ponny";

        UpdateEventRequest updateEventRequest = UpdateEventRequest.builder()
                .maxAttendees(250)
                .build();

        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.ofNullable(event));
        Mockito.when(jwtService.extractUsername(token)).thenReturn(usernameFromToken);

        NotYourEventException notYourEventException = assertThrows(NotYourEventException.class, () -> eventService.updateEvent(updateEventRequest, eventId, token));
        assertEquals("You can update your events only!",notYourEventException.getMessage());

        Mockito.verify(eventRepository).findById(eventId);
        Mockito.verify(jwtService).extractUsername(token);
        assert event != null;
        Mockito.verify(eventRepository,Mockito.never()).save(event);
    }

    @Test
    public void updateEventShouldThrowEventNotFoundExceptionWhenEventDoesNotExist(){
        Long eventId = 1L;
        String token = "token";
        UpdateEventRequest updateEventRequest = UpdateEventRequest.builder()
                .eventName("Updated event name")
                .eventDescription("Updated event description")
                .location("Updated location")
                .maxAttendees(20)
                .eventDate(LocalDate.of(2024, 12, 31))
                .ticketPrice(20.0)
                .eventTarget(EventTarget.EVERYBODY)
                .build();
        Mockito.when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        EventNotFoundException eventNotFoundException = assertThrows(EventNotFoundException.class, () -> eventService.updateEvent(updateEventRequest, eventId, token));
        assertEquals("Event with id " + eventId + " not found",eventNotFoundException.getMessage());

        Mockito.verify(eventRepository).findById(eventId);
        Mockito.verify(jwtService,Mockito.never()).extractUsername(token);
        Mockito.verify(eventRepository,Mockito.never()).save(any(Event.class));
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

    private OrganiserDto createTestOrganiserDto(User user) {
        return OrganiserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}