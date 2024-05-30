package com.marcinsz.eventmanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String eventName;
    private String eventDescription;
    private String location;
    private int maxAttendees;
    private LocalDate eventDate;
    @Enumerated(value = EnumType.STRING)
    private EventStatus eventStatus;
    private double ticketPrice;
    @Enumerated(value = EnumType.STRING)
    private EventType eventType;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;

    @ManyToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinTable(
            name = "participants_events",
            joinColumns = {
                    @JoinColumn(name = "event_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "user_id")
            }
    )
    @JsonManagedReference
    private List<User> participants;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    @JsonBackReference
    private User organizer;


    public Event(String eventName, String eventDescription, String location,
                 int maxAttendees, LocalDate eventDate, EventStatus eventStatus,
                 double ticketPrice, EventType eventType, LocalDateTime createdDate,
                 LocalDateTime modifiedDate, List<User> participants,User organizer) {
        this.eventName = eventName;
        this.eventDescription = eventDescription;
        this.location = location;
        this.maxAttendees = maxAttendees;
        this.eventDate = eventDate;
        this.eventStatus = eventStatus;
        this.ticketPrice = ticketPrice;
        this.eventType = eventType;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.participants = participants;
        this.organizer = organizer;
    }
}
