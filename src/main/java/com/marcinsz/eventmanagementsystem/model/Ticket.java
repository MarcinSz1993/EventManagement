package com.marcinsz.eventmanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "ticket")
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private boolean hasTicket;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonBackReference
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;


}
