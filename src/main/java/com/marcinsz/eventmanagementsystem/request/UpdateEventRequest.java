package com.marcinsz.eventmanagementsystem.request;

import com.marcinsz.eventmanagementsystem.model.EventTarget;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventRequest {

    private String eventName;
    private String eventDescription;
    private String location;
    private Integer maxAttendees;
    private LocalDate eventDate;
    private Double ticketPrice;
    private EventTarget eventTarget;
}
