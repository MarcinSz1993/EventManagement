package com.marcinsz.eventmanagementsystem.service;

import com.marcinsz.eventmanagementsystem.dto.ReviewDto;
import com.marcinsz.eventmanagementsystem.exception.*;
import com.marcinsz.eventmanagementsystem.mapper.ReviewMapper;
import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.PageResponse;
import com.marcinsz.eventmanagementsystem.model.Review;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.repository.EventRepository;
import com.marcinsz.eventmanagementsystem.repository.ReviewRepository;
import com.marcinsz.eventmanagementsystem.repository.UserRepository;
import com.marcinsz.eventmanagementsystem.request.WriteReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final JwtService jwtService;

    public PageResponse<ReviewDto> getReceivedReviews(Authentication connectedUser,int page, int size) {
        User user = (User) connectedUser.getPrincipal();
        Long userId = user.getId();
        Pageable pageable = PageRequest.of(page,size);
        Page<Review> receivedReviews = reviewRepository.findReceivedReviews(userId, pageable);
        List<ReviewDto> receivedReviewsDtoList = receivedReviews.stream()
                .map(ReviewMapper::convertReviewToReviewDto)
                .toList();
        return PageResponse.<ReviewDto>builder()
                .content(receivedReviewsDtoList)
                .number(receivedReviews.getNumber())
                .size(receivedReviews.getSize())
                .totalElements(receivedReviews.getTotalElements())
                .totalPages(receivedReviews.getTotalPages())
                .first(receivedReviews.isFirst())
                .last(receivedReviews.isLast())
                .build();
    }

    public PageResponse<ReviewDto> getAllReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page,size);
        Page<Review> allReviews = reviewRepository.findAll(pageable);
        List<ReviewDto> reviewsDtoList = allReviews.stream()
                .map(ReviewMapper::convertReviewToReviewDto)
                .toList();
        return PageResponse.<ReviewDto>builder()
                .content(reviewsDtoList)
                .number(allReviews.getNumber())
                .size(allReviews.getSize())
                .totalElements(allReviews.getTotalElements())
                .totalPages(allReviews.getTotalPages())
                .first(allReviews.isFirst())
                .last(allReviews.isLast())
                .build();
    }

    public PageResponse<ReviewDto> getAllUserReviews(Long userId,int page, int size) {
        Pageable pageable = PageRequest.of(page,size, Sort.by("degree").descending());
        Page<Review> userReviews = reviewRepository.findAllUserReviews(userId,pageable);
        List<ReviewDto> userReviewsList = userReviews.stream()
                .map(ReviewMapper::convertReviewToReviewDto)
                .toList();
        return new PageResponse<>(
                userReviewsList,
                userReviews.getNumber(),
                userReviews.getSize(),
                userReviews.getNumberOfElements(),
                userReviews.getTotalPages(),
                userReviews.isFirst(),
                userReviews.isLast()
        );

    }

    @Transactional
    public ReviewDto writeReview(WriteReviewRequest writeReviewRequest,String token){
        String username = jwtService.extractUsername(token);
        String requestEventName = writeReviewRequest.getEventName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
        Event event = eventRepository.findByEventName(requestEventName).orElseThrow(() -> new EventNotFoundException(requestEventName));
        isEventFinished(event);
        if(wroteUserReviewToThisEvent(event,user)){
            throw new ReviewAlreadyWrittenException();
        }

        List<Event> userEvents = user.getEvents();
        if (!isParticipant(userEvents, requestEventName) && !event.getParticipants().contains(user)) {
            throw new UserNotParticipantException(username, requestEventName);
        }
        Review review = Review.builder()
                .degree(writeReviewRequest.getDegree())
                .content(writeReviewRequest.getContent())
                .event(event)
                .user(user)
                .build();
        reviewRepository.save(review);
        return ReviewMapper.convertReviewToReviewDto(review);
    }

    @Transactional
    public void deleteReview(Long eventId){
        eventRepository.deleteById(eventId);
    }

    private boolean isParticipant(List<Event> userEvents, String eventName){
        return userEvents.stream()
                .anyMatch(event -> event.getEventName().equals(eventName));
    }

    private boolean wroteUserReviewToThisEvent(Event event,User user){
        return event.getReviews().stream()
                .anyMatch(review -> review.getUser().getId().equals(user.getId()));
    }

    private void isEventFinished(Event event){
        if(event.getEventDate().isAfter(LocalDate.now())) {
            throw new EventNotFinishedException();
        }
    }
}
