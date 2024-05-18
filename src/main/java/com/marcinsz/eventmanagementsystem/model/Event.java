package com.marcinsz.eventmanagementsystem.model;

import jakarta.persistence.*;
import lombok.*;
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
            name = "users_events",
            joinColumns = {
                    @JoinColumn(name = "event_id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "user_id")
            }
    )
    private List<User> users;

}
