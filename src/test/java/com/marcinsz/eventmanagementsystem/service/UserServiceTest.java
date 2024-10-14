package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.exception.BadCredentialsException;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.AuthenticationRequest;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class UserServiceTest {

    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private HttpSession httpSession;
    @Mock
    private HttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldSuccessfullyCreateUser() {
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993, 4, 19))
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .build();

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
        try (MockedStatic<UserMapper> mockedStatic = Mockito.mockStatic(UserMapper.class)) {
            mockedStatic.when(() -> UserMapper.convertCreateUserRequestToUser(createUserRequest)).thenReturn(user);


            when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(user)).thenReturn(user);
            mockedStatic.when(() -> UserMapper.convertUserToUserDto(user)).thenReturn(userDto);
            when(jwtService.generateToken(user)).thenReturn("MockedToken");


            CreateUserResponse createUserResponse = userService.createUser(createUserRequest);

            assertEquals(1L, createUserResponse.getUserDto().getUserId());
            assertEquals("John", createUserResponse.getUserDto().getFirstName());
            assertEquals("Smith", createUserResponse.getUserDto().getLastName());
            assertEquals("john@smith.com", createUserResponse.getUserDto().getEmail());
            assertEquals("johnny", createUserResponse.getUserDto().getUsername());
            assertEquals(LocalDate.of(1993, 4, 19), createUserResponse.getUserDto().getBirthDate());
            assertEquals(Role.USER, createUserResponse.getUserDto().getRole());
            assertEquals("123456789", createUserResponse.getUserDto().getPhoneNumber());
            assertEquals("1234567890", createUserResponse.getUserDto().getAccountNumber());
            assertEquals("ACTIVE", createUserResponse.getUserDto().getAccountStatus());
            assertEquals("MockedToken", createUserResponse.getToken());
        }
    }
    @Test
    public void createUserShouldThrowNullPointerExceptionWithCommunicateWhenInputIsNull(){
        NullPointerException exception = assertThrows(NullPointerException.class, () -> userService.createUser(null));
        assertEquals("CreateUserRequest cannot be null!",exception.getMessage());
    }

    @Test
    public void loginShouldCorrectlyReturnsTokenWhenInputIsCorrect(){
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

        AuthenticationRequest authenticationRequest = new AuthenticationRequest
                ("johnny","encodedPassword");
        AuthenticationResponse expectedResponse = new AuthenticationResponse
                ("mockedToken");
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken
                (authenticationRequest.getUsername(),authenticationRequest.getPassword());

        Mockito.when(httpServletRequest.getSession()).thenReturn(httpSession);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(usernamePasswordAuthenticationToken);
        when(userRepository.findByUsername(authenticationRequest.getUsername())).thenReturn(Optional.ofNullable(user));
        assert user != null;
        when(jwtService.generateToken(user)).thenReturn("mockedToken");

        AuthenticationResponse response = userService.login(authenticationRequest,httpServletRequest);

        assertEquals(expectedResponse.getToken(),response.getToken());
    }

    @Test
    public void loginShouldThrowBadCredentialExceptionWhenAtLeastOneOfCredentialsIsNotCorrect(){

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        AuthenticationRequest authenticationRequest = new AuthenticationRequest("correctLogin","incorrectPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> userService.login(authenticationRequest,request));
        assertEquals("You typed incorrect login or password.",exception.getMessage());
    }

    @Test
    public void getEventsBasedOnUserPreferencesShouldReturnEventsWithMostPopularTargetBasedOnUserPreferences(){
        String token = "token";
        User user = createTestUser();
        Event event1 = createTestEvent(user);
        Event event2 = createTestEvent(user);
        event2.setEventName("Test event 2");

        Event event3 = createTestEvent(user);
        event3.setEventName("Test event 3");
        event3.setEventTarget(EventTarget.ADULTS_ONLY);

        Event event4 = createTestEvent(user);
        event4.setEventName("Test event 4");
        event4.setEventTarget(EventTarget.CHILDREN);

        Event event5 = createTestEvent(user);
        event5.setEventName("Test event 5");

        user.setEvents(List.of(event1, event2, event3,event4,event5));
        String username = user.getUsername();

        EventTarget userPreference = EventTarget.EVERYBODY; //Because most user's events have target EVERYBODY.

        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        Mockito.when(eventRepository.findAllByEventTarget(userPreference)).thenReturn(List.of(event1,event2,event5));

        List<EventDto> actualList = userService.getEventsBasedOnUserPreferences(token);
        assertEquals(3, actualList.size());
        assertEquals(EventTarget.EVERYBODY,actualList.get(0).getEventTarget());
        assertEquals(EventTarget.EVERYBODY,actualList.get(1).getEventTarget());
        assertEquals(EventTarget.EVERYBODY,actualList.get(2).getEventTarget());


        Mockito.verify(jwtService).extractUsername(token);
        Mockito.verify(userRepository).findByUsername(username);
        Mockito.verify(eventRepository).findAllByEventTarget(userPreference);
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



}
