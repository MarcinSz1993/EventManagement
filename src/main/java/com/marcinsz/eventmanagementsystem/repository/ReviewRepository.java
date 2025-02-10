package com.marcinsz.eventmanagementsystem.repository;

import com.marcinsz.eventmanagementsystem.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Query("""
                SELECT review
                FROM Review review
                WHERE review.user.id = :userId
            """)
    Page<Review> findAllUserReviews(@Param("userId") Long userId, Pageable pageable);

    @Query("""
                SELECT review
                FROM Review review
                JOIN review.event event
                WHERE event.organizer.id = :userId
            """)
    Page<Review> findReceivedReviews(@Param("userId") Long userId, Pageable pageable);

}
