package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.exception.BadCredentialsException;
import com.marcinsz.eventmanagementsystem.exception.UserAlreadyExistsException;
import com.marcinsz.eventmanagementsystem.exception.UserHasNoPreferencesYetException;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.mapper.EventMapper;
import com.marcinsz.eventmanagementsystem.mapper.UserMapper;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.AuthenticationRequest;
import com.marcinsz.eventmanagementsystem.request.ChangePasswordRequest;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

        userExists(createUserRequest);

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

        try {
            EventTarget userPreferenceTarget = getPreferedEventTarget(user);
            List<Event> eventsByEventTarget = eventRepository.findAllByEventTarget(userPreferenceTarget);
            Set<Event> setEventsByEventTarget = new HashSet<>(eventsByEventTarget.size());
            setEventsByEventTarget.addAll(eventsByEventTarget);

            return setEventsByEventTarget.stream()
                    .filter(event -> !user.getEvents().contains(event))
                    .map(EventMapper::convertEventToEventDto)
                    .toList();
        } catch (UserHasNoPreferencesYetException e) {
            List<Event> allEventsForEverybody = eventRepository.findAllByEventTarget(EventTarget.EVERYBODY);
            Collections.shuffle(allEventsForEverybody);
            return allEventsForEverybody.stream()
                    .limit(3)
                    .map(EventMapper::convertEventToEventDto)
                    .toList();
        }
    }

    public void changePassword(ChangePasswordRequest changePasswordRequest, Principal connectedUser) {

        log.info("ConnectedUser class: {}", connectedUser.getClass());
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = (UsernamePasswordAuthenticationToken) connectedUser;

        log.info("UsernamePasswordAuthenticationToken getPrincipal class: {}", usernamePasswordAuthenticationToken.getPrincipal().getClass());

        User user = (User) usernamePasswordAuthenticationToken.getPrincipal();

        if (!passwordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect!");
        }

        if (!changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())){
            throw new BadCredentialsException("Passwords are not the same!");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));

        userRepository.save(user);
    }

    private EventTarget getPreferedEventTarget(User user) {
        List<Event> userEvents = user.getEvents();
        return userEvents.stream()
                .collect(Collectors.groupingBy(Event::getEventTarget, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow(UserHasNoPreferencesYetException::new);
    }

    private void userExists(CreateUserRequest createUserRequest){
        if (userRepository.existsByEmail(createUserRequest.getEmail())){
            throw new UserAlreadyExistsException(String.format("User with email %s already exists!", createUserRequest.getEmail()));
        }

        if (userRepository.existsByUsername(createUserRequest.getUsername())){
            throw new UserAlreadyExistsException(String.format("User with username %s already exists!",createUserRequest.getUsername()));
        }

        if (userRepository.existsByPhoneNumber(createUserRequest.getPhoneNumber())){
            throw new UserAlreadyExistsException(String.format("User with phone number %s already exists!", createUserRequest.getPhoneNumber()));
        }

        if (userRepository.existsByAccountNumber(createUserRequest.getAccountNumber())){
            throw new UserAlreadyExistsException(String.format("User with account number %s already exists!", createUserRequest.getAccountNumber()));
        }
    }

    public String getEmailFromToken(String token) {
        String extractedToken = token.substring(7);
        String username = jwtService.extractUsername(extractedToken);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        return user.getEmail();
    }
}


