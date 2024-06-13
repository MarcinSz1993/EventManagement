package com.marcinsz.eventmanagementsystem.request;

import com.marcinsz.eventmanagementsystem.model.EventType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {
    @Column(unique = true)
    @NotBlank(message = "Event name is required")
    private String eventName;
    @NotBlank(message = "You must describe your event")
    private String eventDescription;
    @NotBlank(message = "Location is required")
    private String location;
    @NotNull(message = "Max attendees is required")
    @Positive(message = "A number must be greater than 0")
    private int maxAttendees;
    @FutureOrPresent(message = "A event date cannot be past")
    private LocalDate eventDate;
    @PositiveOrZero(message = "A price must not be below 0")
    private double ticketPrice;
    @NotNull(message = "Event type is required")
    private EventType eventType;
}
