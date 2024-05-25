package com.marcinsz.eventmanagementsystem.dto;

import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.model.EventType;
import com.marcinsz.eventmanagementsystem.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private String eventName;
    private String eventDescription;
    private String eventLocation;
    private int maxAttendees;
    private LocalDate eventDate;
    private EventStatus eventStatus;
    private double ticketPrice;
    private EventType eventType;
    private LocalDateTime createdDate;
    private User organiser;
}
