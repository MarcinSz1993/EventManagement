package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.dto.OrganiserDto;
import com.marcinsz.eventmanagementsystem.dto.UserDto;
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
    public void shouldCreateEventSuccessfullyWhenGivenValidInput(){
        CreateEventRequest createEventRequest = CreateEventRequest.builder()
                .eventName("Example Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .ticketPrice(100)
                .eventTarget(EventTarget.EVERYBODY)
                .build();

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993,4,20))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

        Event event = Event.builder()
                .id(1L)
                .eventName("Example Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100.0)
                .eventTarget(EventTarget.EVERYBODY)
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

        EventDto expectedEventDto = EventDto.builder()
                .eventName("Example Event")
                .eventDescription("Example description")
                .eventLocation("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024,6,20))
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
    public void shouldUpdateEventSuccessfullyWhenGivenValidInput(){
        UpdateEventRequest updateEventRequest = UpdateEventRequest.builder()
                .eventName("Updated name")
                .eventDescription("Updated description")
                .location("Warszawa")
                .maxAttendees(5)
                .eventDate(LocalDate.of(2024,7,1))
                .ticketPrice(10.0)
                .eventTarget(EventTarget.SINGLES)
                .build();

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993,4,20))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

        OrganiserDto organiserDto = OrganiserDto.builder()
                .firstName("John")
                .lastName("Smith")
                .userName("johnny")
                .email("john@smith.com")
                .phoneNumber("123456789")
                .build();

        Event event = Event.builder()
                .id(2L)
                .eventName("Example Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100.0)
                .eventTarget(EventTarget.EVERYBODY)
                .createdDate(LocalDateTime.now())
                .modifiedDate(null)
                .participants(Collections.emptyList())
                .organizer(user)
                .build();

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

            assertEquals(updateEventRequest.getEventName(),actualEventDto.getEventName());

    }
    @Test
    public void shouldSuccessfullyShowAllOrganiserEvents(){
        List<EventDto> expectedEventDtoList = new ArrayList<>();
        List<Event> eventList = new ArrayList<>();

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993,4,20))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

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
                .createdDate(LocalDateTime.of(2024,1,6,10,0))
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
                .createdDate(LocalDateTime.of(2024,2,5,10,0))
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

        EventDto eventDto1 = EventDto.builder()
                .eventName("Example Event")
                .eventDescription("Example description")
                .eventLocation("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024,6,20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100)
                .eventTarget(EventTarget.EVERYBODY)
                .createdDate(LocalDateTime.of(2024,1,6,10,0))
                .organiser(organiserDto)
                .participants(Collections.emptyList())
                .build();

        EventDto eventDto2 = EventDto.builder()
                .eventName("Example Event2")
                .eventDescription("Example description2")
                .eventLocation("Lublin2")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024,7,21))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100)
                .eventTarget(EventTarget.EVERYBODY)
                .createdDate(LocalDateTime.of(2024,2,5,10,0))
                .organiser(organiserDto)
                .participants(Collections.emptyList())
                .build();

        String username = "johnny";

        expectedEventDtoList.add(eventDto1);
        expectedEventDtoList.add(eventDto2);

        eventList.add(event1);
        eventList.add(event2);

        when(userRepository.findByUsername(username)).thenReturn(Optional.ofNullable(user));
        when(eventRepository.findAllByOrganizer(user)).thenReturn(eventList);

        try (MockedStatic<EventMapper> eventMapperMockedStatic = mockStatic(EventMapper.class))
        {
            eventMapperMockedStatic.when(() -> EventMapper.convertListEventToListEventDto(eventList)).thenReturn(eventList);
        }

        List<EventDto> actualEventDtoList = eventService.showAllOrganizerEvents(username);
        assertEquals(expectedEventDtoList.size(),actualEventDtoList.size());
        assertEquals(expectedEventDtoList.get(0).getEventName(),actualEventDtoList.get(0).getEventName());
        assertEquals(expectedEventDtoList.get(0).getEventDescription(),actualEventDtoList.get(0).getEventDescription());
        assertEquals(expectedEventDtoList.get(0).getEventLocation(),actualEventDtoList.get(0).getEventLocation());
        assertEquals(expectedEventDtoList.get(0).getMaxAttendees(),actualEventDtoList.get(0).getMaxAttendees());
        assertEquals(expectedEventDtoList.get(0).getEventDate(),actualEventDtoList.get(0).getEventDate());
        assertEquals(expectedEventDtoList.get(0).getEventStatus(),actualEventDtoList.get(0).getEventStatus());
        assertEquals(expectedEventDtoList.get(0).getTicketPrice(),actualEventDtoList.get(0).getTicketPrice());
        assertEquals(expectedEventDtoList.get(0).getEventTarget(),actualEventDtoList.get(0).getEventTarget());
        assertEquals(expectedEventDtoList.get(0).getCreatedDate(),actualEventDtoList.get(0).getCreatedDate());
        assertEquals(expectedEventDtoList.get(0).getOrganiser(),actualEventDtoList.get(0).getOrganiser());
        assertEquals(expectedEventDtoList.get(0).getParticipants(),actualEventDtoList.get(0).getParticipants());
    }

    @Test
    public void shouldSuccessfullyJoinEventWhenInputIsValid() {

        JoinEventRequest joinEventRequest = JoinEventRequest.builder()
                .email("john@smith.com")
                .build();
        String eventName = "Test Event";
        String token = "token";

        User user = User.builder()
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
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

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

        Event event = Event.builder()
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
    public void shouldSuccessfullyDeleteEvent(){
        Long eventId = 1L;
        String token = "token";

        User user = User.builder()
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
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

        Event event = Event.builder()
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

        String expectedName = event.getEventName();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(jwtService.extractUsername(token)).thenReturn("johnny");

        String actualName = eventService.deleteEvent(eventId, token);

        assertEquals(expectedName,actualName);
        verify(eventRepository).findById(eventId);
        verify(eventRepository).deleteById(eventId);
    }

    @Test
    public void isUserAdultShouldReturnTrueWhenAgeIsMoreOrEquals18(){
        LocalDate dateOfBirth = LocalDate.of(2000,1,1);
        assertTrue(eventService.isUserAdult(dateOfBirth));
    }

    @Test
    public void isUserAdultShouldReturnFalseWhenAgeIsLessThan18(){
        LocalDate dateOfBirth = LocalDate.of(2010,1,1);
        assertFalse(eventService.isUserAdult(dateOfBirth));
    }
}