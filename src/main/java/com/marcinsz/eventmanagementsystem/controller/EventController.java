package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.exception.UserNotFoundException;
import com.marcinsz.eventmanagementsystem.model.DeleteEventResponse;
import com.marcinsz.eventmanagementsystem.model.LeaveEventResponse;
import com.marcinsz.eventmanagementsystem.model.PageResponse;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.request.CreateEventRequest;
import com.marcinsz.eventmanagementsystem.request.JoinEventRequest;
import com.marcinsz.eventmanagementsystem.request.UpdateEventRequest;
import com.marcinsz.eventmanagementsystem.service.EventService;
import com.marcinsz.eventmanagementsystem.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final JwtService jwtService;

    @DeleteMapping("/leaveEvent")
    public ResponseEntity<LeaveEventResponse> leaveEvent(@RequestParam Long eventId, Authentication connectedUser) {
        String eventName = eventService.leaveEvent(eventId, connectedUser);
        return ResponseEntity.ok(LeaveEventResponse.builder()
                .message("You successfully left the event " + eventName)
                .status(HttpStatus.OK.value())
                .build());
    }

    @GetMapping("eventName")
    public ResponseEntity<EventDto> getEventByName(@RequestParam String eventName) {
        return ResponseEntity.ok(eventService.getEventByName(eventName));
    }

    @GetMapping("/joined-completed")
    public ResponseEntity<PageResponse<EventDto>> getCompletedJoinedEvents(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                           @RequestParam(name = "size", defaultValue = "3", required = false) int size,
                                                                           Authentication connectedUser) {
        return ResponseEntity.ok(eventService.getCompletedJoinedEvents(page, size, connectedUser));
    }

    @GetMapping("/joined")
    public ResponseEntity<PageResponse<EventDto>> getFullAndActiveJoinedEvents(@RequestParam(name = "page", defaultValue = "0", required = false) int page,
                                                                               @RequestParam(name = "size", defaultValue = "3", required = false) int size,
                                                                               Authentication connectedUser) {
        return ResponseEntity.ok(eventService.getFullAndActiveJoinedEvents(page, size, connectedUser));
    }

    @GetMapping("{eventId}")
    public ResponseEntity<EventDto> getEvent(@PathVariable Long eventId) {
        EventDto eventDto = eventService.getEventById(eventId);
        return ResponseEntity.ok(eventDto);
    }

    @PostMapping
    public ResponseEntity<EventDto> createEvent(
            @RequestBody @Valid CreateEventRequest createEventRequest,
            @RequestHeader("Authorization") String token) {
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String extractedToken = token.substring(7);
        String username = jwtService.extractUsername(extractedToken);
        User user = eventService.findByUsername(username);
        EventDto event = eventService.createEvent(createEventRequest, user);
        URI newEventLocation = URI.create(String.format("/events/%d", event.getId()));
        return ResponseEntity.created(newEventLocation).body(event);
    }

    @PutMapping
    public ResponseEntity<EventDto> updateEvent(
            @RequestBody @Valid UpdateEventRequest updateEventRequest,
            @RequestParam Long eventId,
            @RequestHeader("Authorization") String token) {
        String extractedToken = token.substring(7);
        EventDto eventDto = eventService.updateEvent(updateEventRequest, eventId, extractedToken);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(eventDto);
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> showAllOrganizerEvents(@RequestParam String username) {
        try {
            List<EventDto> eventDtos = eventService.showAllOrganizerEvents(username);
            if (eventDtos.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .header("Message", "User " + username + " does not have any events")
                        .body(Collections.emptyList());

            }
            return ResponseEntity.ok().body(eventDtos);
        } catch (UserNotFoundException ex) {
            log.info("User with username {} not found", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<EventDto>> getAllEvents(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "4", required = false) int size) {
        return ResponseEntity.ok(eventService.showAllEvents(page, size));
    }

    @PutMapping("/join")
    public ResponseEntity<String> joinEvent(
            @RequestBody @Valid JoinEventRequest joinEventRequest,
            @RequestParam String eventName,
            @RequestHeader("Authorization") String token
    ) {

        try {
            eventService.joinEvent(joinEventRequest, eventName, token);
            return ResponseEntity.ok(String.format("You joined to the event %s.", eventName.toUpperCase()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<DeleteEventResponse> deleteEvent(@RequestParam Long eventId, @RequestHeader("Authorization") String token) {

        String extractedToken = token.substring(7);
        String eventName = eventService.deleteEvent(eventId, extractedToken);
        return ResponseEntity.ok(
                DeleteEventResponse.builder()
                        .message("You deleted the event " + eventName.toUpperCase())
                        .statusCode(HttpStatus.OK.value())
                        .build());
    }
}
