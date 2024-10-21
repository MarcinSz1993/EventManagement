package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.exception.BadCredentialsException;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.AuthenticationRequest;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EventRepository eventRepository;

    @Transactional
    public CreateUserResponse createUser(CreateUserRequest createUserRequest) {
        if (createUserRequest == null) {
            throw new NullPointerException("CreateUserRequest cannot be null!");
        }
        User newUser = UserMapper.convertCreateUserRequestToUser(createUserRequest);
        newUser.setPassword(passwordEncoder.encode(createUserRequest.getPassword()));
        userRepository.save(newUser);
        UserDto userDto = UserMapper.convertUserToUserDto(newUser);
        String token = jwtService.generateToken(newUser);

        return new CreateUserResponse(userDto, token);
    }

    public AuthenticationResponse login(AuthenticationRequest authenticationRequest,
                                        HttpServletRequest httpServletRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );
        } catch (AuthenticationException exception) {
            throw new BadCredentialsException();
        }
        User user = userRepository.findByUsername(authenticationRequest.getUsername()).orElseThrow(() -> new UserNotFoundException(authenticationRequest.getUsername()));
        String token = jwtService.generateToken(user);
        httpServletRequest.getSession().setAttribute("cart",new Cart(new LinkedHashMap<>()));
        return new AuthenticationResponse(token);
    }

    public List<EventDto> getEventsBasedOnUserPreferences(String token) {
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        EventTarget userPreferenceTarget = getPreferedEventTarget(user);
        List<Event> eventsByEventTarget = eventRepository.findAllByEventTarget(userPreferenceTarget);

        return eventsByEventTarget.stream()
                .map(EventMapper::convertEventToEventDto)
                .toList();
    }

    private EventTarget getPreferedEventTarget(User user) {
        List<Event> userEvents = user.getEvents();
        return userEvents.stream()
                .collect(Collectors.groupingBy(Event::getEventTarget, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();
    }
}


