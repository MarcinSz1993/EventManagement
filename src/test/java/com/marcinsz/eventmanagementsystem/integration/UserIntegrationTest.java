package com.marcinsz.eventmanagementsystem.integration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.AuthenticationRequest;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import com.marcinsz.eventmanagementsystem.service.JwtService;
import com.marcinsz.eventmanagementsystem.service.KafkaMessageListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MockBean(JavaMailSender.class)
@MockBean(KafkaMessageListener.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Transactional
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void loginShouldHandleCaseWhenUsernameIsTooShort() throws Exception {
        User user = createTestUser();
        userRepository.save(user);
        AuthenticationRequest authenticationRequest = createTestAuthenticationRequest();
        authenticationRequest.setUsername("user");

        HashMap<String,String> expectedError = new HashMap<>();
        expectedError.put("username","length must be between 5 and 10");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(status().is(400))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedError)))
                .andReturn();
    }

    @Test
    void loginShouldHandleCaseWhenUsernameIsTooLong() throws Exception {
        User user = createTestUser();
        userRepository.save(user);
        AuthenticationRequest authenticationRequest = createTestAuthenticationRequest();
        authenticationRequest.setUsername("tooLongUsername");

        HashMap<String,String> expectedError = new HashMap<>();
        expectedError.put("username","length must be between 5 and 10");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(status().is(400))
                .andExpect(content().json(objectMapper.writeValueAsString(expectedError)))
                .andReturn();
    }

    @Test
    void loginShouldThrowBadCredentialsExceptionsWithSpecifiedCommunicateWhenPasswordNotCorrect() throws Exception {
        User user = createTestUser();
        userRepository.save(user);
        AuthenticationRequest authenticationRequest = createTestAuthenticationRequest();
        authenticationRequest.setPassword("wrongPassword");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isForbidden())
                .andExpect(status().is(403))
                .andExpect(content().string("You typed incorrect login or password."))
                .andReturn();
    }

    @Test
    void loginShouldThrowBadCredentialsExceptionsWithSpecifiedCommunicateWhenUsernameNotCorrect() throws Exception {
        User user = createTestUser();
        userRepository.save(user);
        AuthenticationRequest authenticationRequest = createTestAuthenticationRequest();
        authenticationRequest.setUsername("wrongInput");

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isForbidden())
                .andExpect(status().is(403))
                .andExpect(content().string("You typed incorrect login or password."))
                .andReturn();
    }

    @Test
    void shouldLoginSuccessfully() throws Exception {
        User user = createTestUser();
        userRepository.save(user);
        AuthenticationRequest authenticationRequest = createTestAuthenticationRequest();

        MvcResult result = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        AuthenticationResponse authenticationResponse = objectMapper.readValue(resultAsString, AuthenticationResponse.class);
        String token = authenticationResponse.getToken();

        HttpSession session = result.getRequest().getSession();

        assertNotNull(session);
        Cart cart = (Cart) session.getAttribute("cart");

        Cookie tokenFromCookie = result.getResponse().getCookie("token");

        assertNotNull(tokenFromCookie);
        assertEquals(token, tokenFromCookie.getValue());
        assertEquals("cart",session.getAttributeNames().nextElement());
        assertNotNull(cart);
        assertTrue(cart.getEvents().isEmpty());
    }

    @Test
    void getEventsListBasedOnUserPreferencesShouldReturnAllListOfEventsForEverybodyWhenUserDoesntHavePreferencesYetAndEventListHasLessThan3Events() throws Exception {
        User user = createTestUser();
        User organizer = createTestOrganizer();
        Event event1 = createTestEvent(organizer);
        Event event2 = createTestEvent(organizer);

        event2.setEventName("Event 2");

        userRepository.save(user);
        userRepository.save(organizer);
        eventRepository.save(event1);
        eventRepository.save(event2);

        String token = jwtService.generateToken(user);
        MvcResult result = mockMvc.perform(get("/api/users/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        ArrayList<EventDto> events = objectMapper.readValue(resultAsString, new TypeReference<>() {
        });

        assertNotNull(events);
        assertFalse(events.isEmpty());
        assertEquals(2,events.size());
        assertEquals(EventTarget.EVERYBODY,events.get(0).getEventTarget());
        assertEquals(EventTarget.EVERYBODY,events.get(1).getEventTarget());
    }

    @Test
    void getEventsListBasedOnUserPreferencesShouldReturn3RandomAndDifferentEventsForEverybodyWhenUserDoesntHavePreferencesYet() throws Exception {
        User user = createTestUser();
        User organizer = createTestOrganizer();
        Event event1 = createTestEvent(organizer);
        Event event2 = createTestEvent(organizer);
        Event event3 = createTestEvent(organizer);
        Event event4 = createTestEvent(organizer);
        Event event5 = createTestEvent(organizer);
        event2.setEventName("Event 2");
        event3.setEventName("Event 3");
        event4.setEventName("Event 4");
        event5.setEventName("Event 5");
        event5.setEventTarget(EventTarget.SINGLES);
        userRepository.save(user);
        userRepository.save(organizer);
        eventRepository.saveAll(List.of(event1, event2, event3, event4,event5));

        String token = jwtService.generateToken(user);
        MvcResult result = mockMvc.perform(get("/api/users/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        List<EventDto> events = objectMapper.readValue(resultAsString, new TypeReference<>() {
        });

        assertEquals(3, events.size());
        assertFalse(events.contains(event5));
        assertEquals(EventTarget.EVERYBODY,events.get(0).getEventTarget());
        assertEquals(EventTarget.EVERYBODY,events.get(1).getEventTarget());
        assertEquals(EventTarget.EVERYBODY,events.get(2).getEventTarget());
        assertNotEquals(events.get(0).getEventName(), events.get(1).getEventName());
        assertNotEquals(events.get(0).getEventName(), events.get(1).getEventName());
        assertNotEquals(events.get(0).getEventName(), events.get(2).getEventName());
        assertNotEquals(events.get(1).getEventName(), events.get(2).getEventName());
    }

    @Test
    void getEventsListBasedOnUserPreferencesShouldSuccessfullyHandleSituationWhenUserHasTwoEqualPreferencesByRandomChoosingOneOfThem() throws Exception {
        User organizer = createTestOrganizer();
        userRepository.save(organizer);
        Event event1 = createTestEvent(organizer);
        Event event2 = createTestEvent(organizer);
        Event event3 = createTestEvent(organizer);
        Event event4 = createTestEvent(organizer);
        Event event5 = createTestEvent(organizer);
        Event event6 = createTestEvent(organizer);
        event2.setEventName("Event 2");
        event3.setEventName("Event 3");
        event4.setEventName("Event 4");
        event5.setEventName("Event 5");
        event6.setEventName("Event 6");

        event2.setEventTarget(EventTarget.EVERYBODY);
        event3.setEventTarget(EventTarget.FAMILY);
        event4.setEventTarget(EventTarget.FAMILY);

        event5.setEventTarget(EventTarget.FAMILY);
        event6.setEventTarget(EventTarget.EVERYBODY);

        eventRepository.saveAll(List.of(event1, event2, event3, event4, event5, event6));

        User user = createTestUser();
        user.setEvents(List.of(event1,event2,event3,event4));
        userRepository.save(user);

        event1.setParticipants(List.of(user));
        event2.setParticipants(List.of(user));
        event3.setParticipants(List.of(user));
        event4.setParticipants(List.of(user));

        String token = jwtService.generateToken(user);

        MvcResult result = mockMvc.perform(get("/api/users/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String resultAsString = result.getResponse().getContentAsString();
        List<EventDto> events = objectMapper.readValue(resultAsString, new TypeReference<>() {
        });

        assertFalse(events.isEmpty());
        System.out.println(events);
        assertEquals(1,events.size());
    }

    @Test
    void getEventsListBasedOnUserPreferencesSuccessfully() throws Exception {
        User organizer = createTestOrganizer();
        userRepository.save(organizer);


        Event event1 = createTestEvent(organizer);
        Event event2 = createTestEvent(organizer);
        Event event3 = createTestEvent(organizer);
        Event event4 = createTestEvent(organizer);

        event2.setEventName("Event2");
        event2.setEventTarget(EventTarget.CHILDREN);
        event3.setEventName("Event3");
        event3.setEventTarget(EventTarget.CHILDREN);
        event4.setEventName("Event4");
        event4.setEventTarget(EventTarget.CHILDREN);

        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);
        eventRepository.save(event4);

        User user = createTestUser();
        userRepository.save(user);

        user.setEvents(List.of(event1, event2, event3));

        event1.setParticipants(List.of(user));
        event2.setParticipants(List.of(user));
        event3.setParticipants(List.of(user));


        String token = jwtService.generateToken(user);
        MvcResult result = mockMvc.perform(get("/api/users/preferences")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String jsonResponse = result.getResponse().getContentAsString();

        List<EventDto> events = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertFalse(events.isEmpty());
        assertEquals(1, events.size());
        assertEquals("Event4",events.getFirst().getEventName());
    }

    @Test
    void createUserShouldCreateUserSuccessfullyWhileEdgeCase() throws Exception {
        CreateUserRequest createUserRequestEdgeCase = CreateUserRequest.builder()
                .firstName("Test")
                .lastName("Testing")
                .email("test@test.pl")
                .username("test1")
                .password("test2")
                .birthDate(LocalDate.of(1990, 1, 31))
                .phoneNumber("999999999")
                .accountNumber("1111111111")
                .build();
        User user = UserMapper.convertCreateUserRequestToUser(createUserRequestEdgeCase);
        user.setId(2L);
        UserDto userDto = UserMapper.convertUserToUserDto(user);
        CreateUserResponse expectedCreateUserResponse = CreateUserResponse.builder()
                .userDto(userDto)
                .build();

        String stringExpectedCreateUserResponse = objectMapper.writeValueAsString(expectedCreateUserResponse);
        JsonNode expectedJsonNode = objectMapper.readTree(stringExpectedCreateUserResponse);

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequestEdgeCase)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode actualJsonNode = objectMapper.readTree(result.getResponse().getContentAsString());

        assertEquals(expectedJsonNode.get("userDto"), actualJsonNode.get("userDto"));
        assertTrue(actualJsonNode.get("token").isTextual());
    }

    @Test
    void createUserShouldHandleCaseWhenUserTypeEmailInIncorrectFormat() throws Exception {
        CreateUserRequest createUserRequest = createUserRequest();
        createUserRequest.setEmail("incorrectformatemail.pl");

        HashMap<String, String> expectedError = new HashMap<>();
        expectedError.put("email", "Acceptable pattern is: example@example.com");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andReturn();

        assertEquals(objectMapper.writeValueAsString(expectedError), result.getResponse().getContentAsString());
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
        assertTrue(userRepository.findAll().isEmpty());
        assertEquals(0, userRepository.findAll().size());
    }

    @Test
    void createUserShouldHandleCaseWhenUserTriesToCreateTheAccountWithAlreadyExistingEmail() throws Exception {
        CreateUserRequest createUserRequest = createUserRequest();
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andReturn();

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isConflict())
                .andReturn();

        assertEquals(409, result.getResponse().getStatus());
        assertEquals("User with email " + createUserRequest.getEmail() + " already exists!", result.getResponse().getContentAsString());
    }

    @Test
    void createUserShouldThrowArgumentNotValidExceptionWithSpecifiedCommunicatedWhenThereAreMoreThanOneFieldMissing() throws Exception {
        CreateUserRequest createUserRequest = createUserRequest();
        createUserRequest.setEmail(null);
        createUserRequest.setAccountNumber(null);

        LinkedHashMap<String, String> expectedErrors = new LinkedHashMap<>();
        expectedErrors.put("email", "Email is required");
        expectedErrors.put("accountNumber", "Account number is required");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedErrors)))
                .andReturn();

        assertTrue(userRepository.findAll().isEmpty());
    }

    @Test
    void createUserShouldThrowArgumentNotValidExceptionWithSpecifiedCommunicateWhenAnyRequiredFieldIsMissing() throws Exception {
        CreateUserRequest createUserRequest = createUserRequest();
        createUserRequest.setFirstName(null);
        HashMap<String, String> expectedErrors = new HashMap<>();
        expectedErrors.put("firstName", "First name is required");
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isBadRequest())
                .andReturn();
        assertEquals(objectMapper.writeValueAsString(expectedErrors), result.getResponse().getContentAsString());
        assertEquals(400, result.getResponse().getStatus());
        assertTrue(userRepository.findAll().isEmpty());
    }

    @Test
    void createUserShouldThrowNullPointerExceptionWhenInputIsNull() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(null)))
                .andExpect(status().is4xxClientError())
                .andReturn();

        assertEquals(400, result.getResponse().getStatus());
        assertEquals("Request body cannot be null!", result.getResponse().getContentAsString());
        assertTrue(userRepository.findAll().isEmpty());
    }

    @Test
    void shouldCreateUserAndAddTokenToCookieSuccessfully() throws Exception {
        CreateUserRequest createUserRequest = createUserRequest();

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserRequest)))
                .andExpect(status().isOk())
                .andExpect(cookie().path("token", "/"))
                .andExpect(cookie().httpOnly("token", true))
                .andReturn();

        String tokenValue = Objects.requireNonNull(result.getResponse().getCookie("token")).getValue();

        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("token", Objects.requireNonNull(result.getResponse().getCookie("token")).getName());
        assertNotNull(tokenValue);
        assertFalse(tokenValue.isEmpty());
    }

    private CreateUserRequest createUserRequest() {
        return CreateUserRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993, 4, 19))
                .phoneNumber("123456789")
                .accountNumber("1234567890")
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

    private User createTestOrganizer(){
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

    private AuthenticationRequest createTestAuthenticationRequest(){
        return AuthenticationRequest.builder()
                .username("johnny")
                .password("encodedPassword")
                .build();
    }
}
