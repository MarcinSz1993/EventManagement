package com.marcinsz.eventmanagementsystem.request;

import com.marcinsz.eventmanagementsystem.model.EventTarget;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
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
    @Positive(message = "Max attendees must be greater than 0")
    private Integer maxAttendees;
    @Future(message = "New event date cannot be past")
    private LocalDate eventDate;
    private Double ticketPrice;
    private EventTarget eventTarget;
}
