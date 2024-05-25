package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import com.marcinsz.eventmanagementsystem.service.EventService;
import com.marcinsz.eventmanagementsystem.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/event")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    @PostMapping("/")
    public EventDto createEvent(@RequestBody @Valid CreateEventRequest createEventRequest,
                                @CookieValue String token){

        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow();
        return eventService.createEvent(createEventRequest,user);
    }

    @GetMapping("/")
    public List<Event> showAllUserEvents(@RequestParam String username){
        return eventService.showAllOrganizerEvents(username);
    }
}
