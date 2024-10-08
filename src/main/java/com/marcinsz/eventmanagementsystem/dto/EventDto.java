package com.marcinsz.eventmanagementsystem.dto;

import com.marcinsz.eventmanagementsystem.model.EventStatus;
import com.marcinsz.eventmanagementsystem.model.EventTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private Long id;
    private String eventName;
    private String eventDescription;
    private String eventLocation;
    private int maxAttendees;
    private LocalDate eventDate;
    private EventStatus eventStatus;
    private double ticketPrice;
    private EventTarget eventTarget;
    private LocalDateTime createdDate;
    private OrganiserDto organiser;
    private List<UserDto> participants;
}
