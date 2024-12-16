package com.marcinsz.eventmanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import com.marcinsz.eventmanagementsystem.request.UpdateEventRequest;
import com.marcinsz.eventmanagementsystem.service.JwtService;
import com.marcinsz.eventmanagementsystem.service.KafkaMessageListener;
import com.marcinsz.eventmanagementsystem.service.NotificationService;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@MockBean(KafkaMessageListener.class)
@MockBean(NotificationService.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EventIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
    }

    @Test
    public void eventUpdateShouldThrowAndHandleEventNotFoundWhenEventDoesNotExist() throws Exception {
        User user = createTestUser();
        UpdateEventRequest updateEventRequest = createUpdateEventRequest();
        String token = jwtService.generateToken(user);
        Cookie cookie = new Cookie("token", token);

        long notExistingEvent = 1L;

        userRepository.save(user);

        mockMvc.perform(put("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateEventRequest))
                .param("eventId", Long.toString(notExistingEvent))
                .cookie(cookie))
                .andExpect(status().isNotFound());
               // .andExpect(content().string("Event with id 1 not found"));
    }

    @Test
    public void eventUpdateShouldThrowNotYourEventExceptionWhenUserTriesToUpdateNotHisEvent() throws Exception {
        User organizer = createTestOrganizer();
        Event event = createTestEvent(organizer);
        User user = createTestUser();
        UpdateEventRequest updateEventRequest = createUpdateEventRequest();
        String token = jwtService.generateToken(user);
        Cookie cookie = new Cookie("token", token);

        userRepository.save(user);
        userRepository.save(organizer);
        eventRepository.save(event);

        Long eventId = event.getId();

        mockMvc.perform(put("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateEventRequest))
                        .param("eventId", eventId.toString())
                .cookie(cookie))
                .andExpect(status().isConflict())
                .andExpect(content().string("You can update your events only!"));
    }

    @Test
    public void shouldUpdateEventSuccessfully() throws Exception {
        User organizer = createTestOrganizer();
        Event event = createTestEvent(organizer);
        UpdateEventRequest updateEventRequest = createUpdateEventRequest();
        userRepository.save(organizer);
        eventRepository.save(event);
        Long eventId = event.getId();
        String token = jwtService.generateToken(organizer);
        Cookie cookie = new Cookie("token", token);

        EventDto expectedEventDto = EventMapper.convertEventToEventDto(event);
        expectedEventDto.setEventName(updateEventRequest.getEventName());
        expectedEventDto.setEventDescription(updateEventRequest.getEventDescription());
        expectedEventDto.setEventLocation(updateEventRequest.getLocation());
        expectedEventDto.setMaxAttendees(updateEventRequest.getMaxAttendees());
        expectedEventDto.setEventDate(updateEventRequest.getEventDate());
        expectedEventDto.setTicketPrice(updateEventRequest.getTicketPrice());
        expectedEventDto.setEventTarget(updateEventRequest.getEventTarget());

        mockMvc.perform(put("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(cookie)
                        .content(objectMapper.writeValueAsString(updateEventRequest))
                        .param("eventId", String.valueOf(eventId)))
                .andExpect(status().isAccepted())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedEventDto)))
                .andReturn();
    }

    @Test
    public void createEventShouldReturnUnauthorizedStatusWhenJwtTokenIsInvalid() throws Exception {
        CreateEventRequest createEventRequest = createEventRequest();
        String invalidToken = "invalidToken";
        Cookie cookie = new Cookie("token",invalidToken);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventRequest))
                        .cookie(cookie))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid JWT token.Invalid compact JWT string: Compact JWSs must contain exactly 2 period characters, and compact JWEs must contain exactly 4.  Found: 0"));
    }

    @Test
    public void createEventShouldReturnStatusForbiddenWhenAuthorizationTokenExpired() throws Exception {
        CreateEventRequest createEventRequest = createEventRequest();
        User user = createTestUser();
        String expiredToken = generateExpiredToken(user);
        Cookie cookie = new Cookie("token",expiredToken);

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createEventRequest))
                .cookie(cookie))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Expired JWT token. Please log in once again."));
    }

    @Test
    public void createEventShouldReturnStatusUnauthorizedWhenThereIsNoCookieInRequest() throws Exception {
        CreateEventRequest createEventRequest = createEventRequest();

        mockMvc.perform(post("/api/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createEventRequest)))
                .andExpect(status().is(401))
                .andExpect(content().string("Missing required cookie."))
                .andReturn();
    }


    @Test
    public void createEventShouldReturnValidationErrorsWhenCreatingEventRequestContainsInvalidFields() throws Exception {
        User user = createTestUser();
        String token = jwtService.generateToken(user);
        CreateEventRequest createEventRequest = new CreateEventRequest();
        createEventRequest.setEventName("");
        createEventRequest.setEventDescription("");
        createEventRequest.setLocation("");
        createEventRequest.setMaxAttendees(-10);
        createEventRequest.setEventDate(LocalDate.now().minusDays(1));
        createEventRequest.setTicketPrice(-100.0);
        createEventRequest.setEventTarget(null);

        LinkedHashMap<String, String> expectedResult = new LinkedHashMap<>();
        expectedResult.put("eventName", "Event name is required");
        expectedResult.put("eventDescription", "You must describe your event");
        expectedResult.put("location", "Location is required");
        expectedResult.put("maxAttendees", "A number must be greater than 0");
        expectedResult.put("eventDate", "A event date cannot be past");
        expectedResult.put("ticketPrice", "A price must not be below 0");
        expectedResult.put("eventTarget", "Event target is required");

        Cookie cookie = new Cookie("token", token);

                mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventRequest))
                        .cookie(cookie))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedResult)))
                .andReturn();
    }

    @Test
    public void createEventShouldCreateAndSaveEventInDatabaseSuccessfully() throws Exception {
        CreateEventRequest createEventRequest = createEventRequest();
        User user = createTestUser();
        String token = jwtService.generateToken(user);
        Cookie cookie = new Cookie("token", token);
        Event event = createTestEvent(user);
        EventDto expectedEventDto = createTestEventDto(event);
        userRepository.save(user);

        MvcResult result = mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createEventRequest))
                        .cookie(cookie))
                .andExpect(status().isCreated())
                .andReturn();
        System.out.println(result.getResponse().getHeader("Location"));

        String resultAsString = result.getResponse().getContentAsString();
        EventDto actualEventDto = objectMapper.readValue(resultAsString, EventDto.class);
        expectedEventDto.setId(actualEventDto.getId());

        assertEquals(String.format("/events/%d",actualEventDto.getId()),result.getResponse().getHeader("Location"));

        assertEquals(expectedEventDto.getId(), actualEventDto.getId());
        assertEquals(expectedEventDto.getEventName(), actualEventDto.getEventName());
        assertEquals(expectedEventDto.getEventDescription(), actualEventDto.getEventDescription());
        assertEquals(expectedEventDto.getEventDate(), actualEventDto.getEventDate());
        assertEquals(expectedEventDto.getEventLocation(), actualEventDto.getEventLocation());

    }

    @Test
    public void getEventShouldReturnEvent() throws Exception {
        User organizer = createTestOrganizer();
        Event event = createTestEvent(organizer);

        eventRepository.save(event);
        EventDto eventDto = createTestEventDto(event);
        Long eventId = event.getId();
        MvcResult result = mockMvc.perform(get("/api/events/{eventId}", eventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(eventDto)))
                .andReturn();

        String actualPath = result.getRequest().getRequestURI();

        assertEquals("/api/events/1", actualPath);
    }

    @Test
    public void getEventShouldHandleNotFoundExceptionWhenEventIdIsNotCorrect() throws Exception {
        Long notFoundEventId = 1L;
        mockMvc.perform(get("/api/events/{eventId}", notFoundEventId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Event with id 1 not found"))
                .andReturn();
    }

    private Event createTestEvent(User organizer) {
        return Event.builder()
                .eventName("Test Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2025, 6, 20))
                .eventStatus(EventStatus.ACTIVE)
                .ticketPrice(100.0)
                .eventTarget(EventTarget.EVERYBODY)
                .createdDate(LocalDateTime.of(2024, 1, 6, 10, 0))
                .modifiedDate(null)
                .participants(new ArrayList<>())
                .organizer(organizer)
                .build();
    }

    private User createTestUser() {
        return User.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("$2a$12$OGMHzPw4EFl4YFFAGCJoc..hOUWS3xn3VWfw2hiAeDYJvuRn0RbI6")
                .birthDate(LocalDate.of(1993, 4, 19))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(new ArrayList<>())
                .build();
    }

    private User createTestOrganizer() {
        return User.builder()
                .firstName("Tom")
                .lastName("Willson")
                .email("tom@willson.com")
                .username("tommy")
                .password("encodedPassword")
                .birthDate(LocalDate.of(1990, 4, 19))
                .role(Role.USER)
                .phoneNumber("123456788")
                .accountNumber("1234567899")
                .accountStatus("ACTIVE")
                .events(new ArrayList<>())
                .build();
    }

    private EventDto createTestEventDto(Event event) {
        return EventMapper.convertEventToEventDto(event);
    }

    private CreateEventRequest createEventRequest() {
        return CreateEventRequest.builder()
                .eventName("Test Event")
                .eventDescription("Example description")
                .location("Lublin")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2025, 6, 20))
                .ticketPrice(100)
                .eventTarget(EventTarget.EVERYBODY)
                .build();
    }

    private UpdateEventRequest createUpdateEventRequest(){
        return UpdateEventRequest.builder()
                .eventName("Update event name.")
                .eventDescription("Update event description.")
                .location("Updated location")
                .maxAttendees(10)
                .eventDate(LocalDate.of(2025, 6, 20))
                .ticketPrice(199.99)
                .eventTarget(EventTarget.CHILDREN)
                .build();
    }

    public String generateExpiredToken(User user){
        return Jwts
                .builder()
                .subject(user.getUsername())
                .claim("role",user.getRole().name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(jwtService.getSigningKey())
                .compact();
    }
}
