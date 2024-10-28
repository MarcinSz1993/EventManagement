package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.ReviewDto;
import com.marcinsz.eventmanagementsystem.exception.EventNotFinishedException;
import com.marcinsz.eventmanagementsystem.exception.ReviewAlreadyWrittenException;
import com.marcinsz.eventmanagementsystem.exception.UserNotParticipantException;
import com.marcinsz.eventmanagementsystem.model.*;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.ReviewRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.WriteReviewRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ReviewService reviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void writeReviewShouldSuccessfullyWriteAndSaveReview() {
        User reviewer = createTestReviewer();
        Event event = createTestEvent();
        reviewer.setEvents(List.of(event));
        WriteReviewRequest writeReviewRequest = createTestWriteReviewRequest();
        String token = "token";
        String username = reviewer.getUsername();
        String eventName = writeReviewRequest.getEventName();

        Review expectedReview = Review.builder()
                .id(1L)
                .degree(writeReviewRequest.getDegree())
                .content(writeReviewRequest.getContent())
                .event(event)
                .user(reviewer)
                .build();

        mockingBasicDependencies(token, username, reviewer, eventName, event);

        ReviewDto actualReview = reviewService.writeReview(writeReviewRequest, token);

        assertEquals(expectedReview.getDegree(), actualReview.getDegree());
        assertEquals(expectedReview.getContent(), actualReview.getContent());

        Mockito.verify(jwtService,Mockito.times(1)).extractUsername(token);
        Mockito.verify(userRepository,Mockito.times(1)).findByUsername(username);
        Mockito.verify(eventRepository,Mockito.times(1)).findByEventName(eventName);
        Mockito.verify(reviewRepository,Mockito.times(1)).save(Mockito.any(Review.class));
    }

    @Test
    public void shouldWriteReviewThrowReviewAlreadyWrittenExceptionWhenReviewerWantsToWriteReviewMoreThanOnce(){
        User reviewer = createTestReviewer();
        Event event = createTestEvent();
        reviewer.setEvents(List.of(event));
        WriteReviewRequest writeReviewRequest = createTestWriteReviewRequest();
        Review review = Review.builder()
                .id(1L)
                .degree(writeReviewRequest.getDegree())
                .content(writeReviewRequest.getContent())
                .event(event)
                .user(reviewer)
                .build();
        event.setReviews(List.of(review));

        String token = "token";
        String username = reviewer.getUsername();
        String eventName = writeReviewRequest.getEventName();

        mockingBasicDependencies(token, username, reviewer, eventName, event);

        ReviewAlreadyWrittenException reviewAlreadyWrittenException = assertThrows(ReviewAlreadyWrittenException.class, () -> reviewService.writeReview(writeReviewRequest, token));
        assertEquals("You have already written a review for this event.", reviewAlreadyWrittenException.getMessage());

        Mockito.verify(jwtService,Mockito.times(1)).extractUsername(token);
        Mockito.verify(userRepository,Mockito.times(1)).findByUsername(username);
        Mockito.verify(eventRepository,Mockito.times(1)).findByEventName(eventName);
    }
    @Test
    public void writeReviewShouldThrowEventNotFinishedExceptionWhenReviewerWantsToWriteReviewBeforeEventIsFinished(){
        User reviewer = createTestReviewer();
        Event event = createTestEvent();
        reviewer.setEvents(List.of(event));
        WriteReviewRequest writeReviewRequest = createTestWriteReviewRequest();
        String token = "token";
        event.setEventDate(LocalDate.now().plusDays(1));
        mockingBasicDependencies(token,reviewer.getUsername(),reviewer,event.getEventName(),event);

        EventNotFinishedException eventNotFinishedException = assertThrows(EventNotFinishedException.class, () -> reviewService.writeReview(writeReviewRequest, token));
        assertEquals("Event has been not finished yet", eventNotFinishedException.getMessage());

        Mockito.verify(reviewRepository,Mockito.never()).save(Mockito.any(Review.class));

    }

    @Test
    public void writeReviewShouldThrowUserNotParticipantExceptionWhenUserIsNotParticipantOfTheEventAndHeWantsToReview(){
        User reviewer = createTestReviewer();
        Event event = createTestEvent();
        event.setParticipants(Collections.emptyList());
        String token = "token";
        String username = reviewer.getUsername();
        WriteReviewRequest writeReviewRequest = createTestWriteReviewRequest();

        mockingBasicDependencies(token,username,reviewer,event.getEventName(),event);

        assertThrows(UserNotParticipantException.class,() -> reviewService.writeReview(writeReviewRequest,token));
        Mockito.verify(reviewRepository,Mockito.never()).save(Mockito.any(Review.class));
    }

    private WriteReviewRequest createTestWriteReviewRequest() {
        return WriteReviewRequest.builder()
                .eventName("Test Event")
                .content("Test Content")
                .degree(4)
                .build();
    }

    private void mockingBasicDependencies(String token, String username, User reviewer, String eventName, Event event) {
        Mockito.when(jwtService.extractUsername(token)).thenReturn(username);
        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(reviewer));
        Mockito.when(eventRepository.findByEventName(eventName)).thenReturn(Optional.of(event));
    }

    private User createTestReviewer() {
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
                .events(Collections.emptyList())
                .build();
    }

    private Event createTestEvent() {
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
                .organizer(null)
                .reviews(Collections.emptyList())
                .build();
    }
}