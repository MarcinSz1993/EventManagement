package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import com.marcinsz.eventmanagementsystem.request.JoinEventRequest;
import com.marcinsz.eventmanagementsystem.request.UpdateEventRequest;
import com.marcinsz.eventmanagementsystem.service.EventService;
import com.marcinsz.eventmanagementsystem.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/events")
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
    @PutMapping("/update")
    public EventDto updateEvent(@RequestBody UpdateEventRequest updateEventRequest,
                                @RequestParam Long eventId,
                                @CookieValue String token){
        return eventService.updateEvent(updateEventRequest,eventId,token);
    }

    @GetMapping("/")
    public List<Event> showAllUserEvents(@RequestParam String username){
        return eventService.showAllOrganizerEvents(username);
    }
    @PutMapping("/join")
    public ResponseEntity<String> joinEvent(@RequestBody @Valid JoinEventRequest joinEventRequest,
                                            @RequestParam String eventName){
        try {
            eventService.joinEvent(joinEventRequest,eventName);
            return ResponseEntity.ok("You joined to the event " + eventName.toUpperCase() +".");
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
    @DeleteMapping("/")
    public ResponseEntity<String> deleteEvent(@RequestParam Long eventId,
                                              @CookieValue String token){
        try{
            String eventName = eventService.deleteEvent(eventId, token);
            return ResponseEntity.ok("You deleted event " + eventName);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
        }
    }
}
