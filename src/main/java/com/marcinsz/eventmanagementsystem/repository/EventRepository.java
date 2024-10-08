package com.marcinsz.eventmanagementsystem.repository;

import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event,Long> {

    List<Event> findAllByOrganizer(User organizer);

    Optional<Event> findByEventName(String eventName);

    @Query("SELECT e from Event e where e.eventStatus = :status")
    List<Event> findAllByActiveEventStatus(@Param("status") EventStatus status);
}
