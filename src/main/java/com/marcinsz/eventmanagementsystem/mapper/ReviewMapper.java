package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.ReviewDto;
import com.marcinsz.eventmanagementsystem.model.Review;

public class ReviewMapper {

    public static ReviewDto convertReviewToReviewDto(Review review) {
        return ReviewDto.builder()
                .eventName(review.getEvent().getEventName())
                .degree(review.getDegree())
                .content(review.getContent())
                .reviewer(review.getUser().getUsername())
                .build();
    }
}
