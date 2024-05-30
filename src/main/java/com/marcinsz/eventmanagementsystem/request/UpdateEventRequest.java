package com.marcinsz.eventmanagementsystem.request;

import com.marcinsz.eventmanagementsystem.model.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {

    private String eventName;
    private String eventDescription;
    private String location;
    private Integer maxAttendees;
    private LocalDate eventDate;
    private Double ticketPrice;
    private EventType eventType;
}
