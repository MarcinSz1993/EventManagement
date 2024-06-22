package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.exception.BadCredentialsException;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.AuthenticationResponse;
import com.marcinsz.eventmanagementsystem.model.CreateUserResponse;
import com.marcinsz.eventmanagementsystem.model.Role;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.AuthenticationRequest;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Collections;
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
    private JwtService jwtService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //@Test
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

            when(userMapper.convertCreateUserRequestToUser(createUserRequest)).thenReturn(user);
            when(passwordEncoder.encode(createUserRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.convertUserToUserDto(user)).thenReturn(userDto);
            when(jwtService.generateToken(user)).thenReturn("MockedToken");

            CreateUserResponse createUserResponse = userService.createUser(createUserRequest);

            assertEquals(1L, createUserResponse.getUserDto().getUser_id());
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

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(usernamePasswordAuthenticationToken);
        when(userRepository.findByUsername(authenticationRequest.getUsername())).thenReturn(Optional.ofNullable(user));
        when(jwtService.generateToken(user)).thenReturn("mockedToken");

        AuthenticationResponse response = userService.login(authenticationRequest);

        assertEquals(expectedResponse.getToken(),response.getToken());
    }

    @Test
    public void loginShouldThrowBadCredentialExceptionWhenAtLeastOneOfCredentialsIsNotCorrect(){

        AuthenticationRequest authenticationRequest = new AuthenticationRequest("correctLogin","incorrectPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new BadCredentialsException());

        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> userService.login(authenticationRequest));
        assertEquals("You typed incorrect login or password.",exception.getMessage());
    }

}
