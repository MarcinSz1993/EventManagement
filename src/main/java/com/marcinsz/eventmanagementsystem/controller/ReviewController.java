package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.dto.ReviewDto;
import com.marcinsz.eventmanagementsystem.request.WriteReviewRequest;
import com.marcinsz.eventmanagementsystem.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    //todo napisać metodę do pobierania wszysktich recenzji.
    //todo napisać metodę do pobierania recenzji eventów danego użytkownika.

    private final ReviewService reviewService;

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
