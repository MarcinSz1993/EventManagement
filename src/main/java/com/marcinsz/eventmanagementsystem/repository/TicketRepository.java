package com.marcinsz.eventmanagementsystem.repository;

import com.marcinsz.eventmanagementsystem.model.Event;
import com.marcinsz.eventmanagementsystem.model.Ticket;
import com.marcinsz.eventmanagementsystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByUser_IdAndEvent_Id(Long userId,Long eventId);

    boolean existsTicketByUserAndEvent(User user, Event event);

}
