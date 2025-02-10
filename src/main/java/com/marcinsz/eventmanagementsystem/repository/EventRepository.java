package com.marcinsz.eventmanagementsystem.repository;

import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.model.EventTarget;
import com.marcinsz.eventmanagementsystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByEventName(String eventName);

    List<Event> findAllByOrganizer(User organizer);

    Optional<Event> findByEventName(String eventName);

    @Query("SELECT e from Event e where e.eventStatus = :status")
    List<Event> findAllByActiveEventStatus(@Param("status") EventStatus status);

    @Query("""
            SELECT event FROM Event event
            WHERE event.eventTarget = :eventTarget
            AND event.eventStatus = 'ACTIVE'
            AND event.organizer.id != :userId
            AND NOT EXISTS (
                SELECT 1 FROM event.participants p WHERE p.id = :userId
            )
            """)
    Page<Event> findAllByEventTargetAndActiveStatusAndNotJoined(@Param("eventTarget") EventTarget eventTarget,
                                                                @Param("userId") Long userId,
                                                                Pageable pageable);

    @Query("""
            SELECT event FROM Event event
            join event.participants p
            WHERE p.id = :userId
            AND (event.eventStatus = 'ACTIVE'
            OR event.eventStatus = 'FULL')
            """)
    Page<Event> findFullAndActiveEventsJoinedByUser(Long userId, Pageable pageable);

    @Query("""
            SELECT event FROM Event event
            JOIN event.participants p
            WHERE p.id = :userId
            AND event.eventStatus = 'COMPLETED'
            AND event.organizer.id != :userId
            """)
    Page<Event> findCompletedEventsJoinedByUser(Long userId, Pageable pageable);

    @Query("""
           SELECT event FROM Event event
           JOIN event.participants p
           WHERE event.id = :eventId
           AND p.id = :userId
           """)
    Optional<Event> findByIdAndUserId(@Param("eventId") Long eventId,@Param("userId") Long userId);
}
