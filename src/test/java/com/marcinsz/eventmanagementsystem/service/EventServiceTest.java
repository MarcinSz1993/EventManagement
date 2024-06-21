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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
    @Mock
    private EventMapper eventMapper;
    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //@Test
    public void shouldCreateEventSuccessfullyWhenGivenValidInput() {
        // Given
        CreateEventRequest createEventRequest = CreateEventRequest.builder()
                .eventName("Example Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .ticketPrice(100)
                .eventType(EventType.EVERYBODY)
                .build();

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
                .eventName(createEventRequest.getEventName())
                .eventDescription(createEventRequest.getEventDescription())
                .location(createEventRequest.getLocation())
                .maxAttendees(createEventRequest.getMaxAttendees())
                .eventDate(createEventRequest.getEventDate())
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(createEventRequest.getTicketPrice())
                .eventType(createEventRequest.getEventType())
                .createdDate(LocalDateTime.now())
                .organizer(user)
                .build();

        EventDto expectedEventDto = EventDto.builder()
                .eventName("Different name") // Ustawienie innej nazwy
                .eventDescription("Example description")
                .eventLocation("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2024, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100)
                .createdDate(event.getCreatedDate())
                .organiser(OrganiserDto.builder()
                        .firstName("John")
                        .lastName("Smith")
                        .userName("johnny")
                        .email("john@smith.com")
                        .phoneNumber("123456789")
                        .build())
                .participants(Collections.emptyList())
                .build();

        // Mocking
        when(eventMapper.convertCreateEventRequestToEvent(createEventRequest)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.convertEventToEventDto(event)).thenReturn(expectedEventDto);

        doNothing().when(kafkaMessageProducer).sendMessageToTopic(expectedEventDto);

        // When
        EventDto actualEventDto = eventService.createEvent(createEventRequest, user);

        // Debugging
        System.out.println("Actual Event DTO: " + actualEventDto);

        // Then
        assertNotNull(actualEventDto, "Event DTO should not be null");

        assertNotEquals(expectedEventDto.getEventName(), actualEventDto.getEventName(), "Event name should not match");

        assertEquals(createEventRequest.getEventName(), actualEventDto.getEventName(), "Event name should match the request");
        assertEquals(createEventRequest.getEventDescription(), actualEventDto.getEventDescription(), "Event description should match the request");
        assertEquals(expectedEventDto.getEventLocation(), actualEventDto.getEventLocation(), "Event location should match");
        assertEquals(expectedEventDto.getMaxAttendees(), actualEventDto.getMaxAttendees(), "Max attendees should match");
        assertEquals(expectedEventDto.getEventDate(), actualEventDto.getEventDate(), "Event date should match");
        assertEquals(expectedEventDto.getEventStatus(), actualEventDto.getEventStatus(), "Event status should match");
        assertEquals(expectedEventDto.getTicketPrice(), actualEventDto.getTicketPrice(), "Ticket price should match");
        assertEquals(expectedEventDto.getCreatedDate(), actualEventDto.getCreatedDate(), "Created date should match");
        assertEquals(expectedEventDto.getOrganiser(), actualEventDto.getOrganiser(), "Organiser should match");
        assertEquals(expectedEventDto.getParticipants(), actualEventDto.getParticipants(), "Participants should match");

        verify(eventRepository).save(event);
        verify(kafkaMessageProducer).sendMessageToTopic(expectedEventDto);
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
                .eventType(EventType.SINGLES)
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
                .eventType(EventType.EVERYBODY)
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
                .eventType(updateEventRequest.getEventType())
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
        when(eventMapper.convertEventToEventDto(event)).thenReturn(expectedEventDto);

        EventDto actualEventDto = eventService.updateEvent(updateEventRequest, eventIdToUpdate, token);

        assertEquals(updateEventRequest.getEventName(),actualEventDto.getEventName());
        assertEquals(updateEventRequest.getEventDescription(),actualEventDto.getEventDescription());


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
                .eventType(EventType.EVERYBODY)
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
                .eventType(EventType.EVERYBODY)
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
                .eventType(EventType.EVERYBODY)
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
                .eventType(EventType.EVERYBODY)
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
        when(eventMapper.convertListEventToListEventDto(eventList)).thenReturn(expectedEventDtoList);


        List<EventDto> actualEventDtoList = eventService.showAllOrganizerEvents(username);
        assertEquals(expectedEventDtoList.size(),actualEventDtoList.size());
        assertEquals(expectedEventDtoList.get(0).getEventName(),actualEventDtoList.get(0).getEventName());
        assertEquals(expectedEventDtoList.get(0).getEventDescription(),actualEventDtoList.get(0).getEventDescription());
        assertEquals(expectedEventDtoList.get(0).getEventLocation(),actualEventDtoList.get(0).getEventLocation());
        assertEquals(expectedEventDtoList.get(0).getMaxAttendees(),actualEventDtoList.get(0).getMaxAttendees());
        assertEquals(expectedEventDtoList.get(0).getEventDate(),actualEventDtoList.get(0).getEventDate());
        assertEquals(expectedEventDtoList.get(0).getEventStatus(),actualEventDtoList.get(0).getEventStatus());
        assertEquals(expectedEventDtoList.get(0).getTicketPrice(),actualEventDtoList.get(0).getTicketPrice());
        assertEquals(expectedEventDtoList.get(0).getEventType(),actualEventDtoList.get(0).getEventType());
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
                .user_id(1L)
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
                .eventType(EventType.EVERYBODY)
                .createdDate(LocalDateTime.of(2024, 1, 6, 10, 0))
                .modifiedDate(null)
                .participants(new ArrayList<>())
                .organizer(user)
                .build();

        when(jwtService.extractUsername(token)).thenReturn("johnny");
        when(userRepository.findByUsername("johnny")).thenReturn(Optional.of(user));
        when(eventRepository.findByEventName(eventName)).thenReturn(Optional.of(event));
        when(userRepository.findByEmail(joinEventRequest.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.convertUserToUserDto(user)).thenReturn(userDto);

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
                .eventType(EventType.EVERYBODY)
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