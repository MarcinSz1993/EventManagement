package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.dto.ReviewDto;
import com.marcinsz.eventmanagementsystem.model.PageResponse;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.request.WriteReviewRequest;
import com.marcinsz.eventmanagementsystem.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/received")
    public ResponseEntity<PageResponse<ReviewDto>> getReceivedReviews(Authentication connectedUser,
                                                                      @RequestParam(name = "page",defaultValue = "0",required = false) int page,
                                                                      @RequestParam(name = "size",defaultValue = "3",required = false) int size){
        return ResponseEntity.ok(reviewService.getReceivedReviews(connectedUser,page,size));
    }

    @GetMapping("/all")
    public ResponseEntity<PageResponse<ReviewDto>> getAllReviews(
            @RequestParam(name = "page",defaultValue = "0",required = false) int page,
            @RequestParam(name = "size",defaultValue = "3",required = false) int size) {
        return ResponseEntity.ok(reviewService.getAllReviews(page,size));
    }

    @GetMapping("/written")
    public ResponseEntity<PageResponse<ReviewDto>> getReviewsWrittenByUser(Authentication connectedUser,
                                                                           @RequestParam(name = "page",defaultValue = "0",required = false) int page,
                                                                           @RequestParam(name = "size",defaultValue = "3",required = false) int size) {
        User user = (User) connectedUser.getPrincipal();
        Long userId = user.getId();
        return ResponseEntity.ok(reviewService.getAllUserReviews(userId,page,size));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> writeReview(
                                                @Valid @RequestBody WriteReviewRequest writeReviewRequest,
                                                @RequestHeader("Authorization") String token){
        String extractedToken = token.substring(7);
        ReviewDto reviewDto = reviewService.writeReview(writeReviewRequest, extractedToken);
        return ResponseEntity.ok(reviewDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteReview(@PathVariable(name = "id") Long eventId){
        reviewService.deleteReview(eventId);
        return ResponseEntity.ok().body("Review with id " + eventId + " deleted successfully.");
    }
}
