package com.marcinsz.eventmanagementsystem.request;

import com.marcinsz.eventmanagementsystem.model.EventType;
import com.marcinsz.eventmanagementsystem.model.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {
    @NotBlank
    private String eventName;
    @NotBlank
    private String eventDescription;
    @NotBlank
    private String location;
    @Positive
    @NotNull
    private int maxAttendees;
    @FutureOrPresent
    private LocalDate eventDate;
    @PositiveOrZero
    private double ticketPrice;
    @NotNull
    private EventType eventType;
    private User organizer;
}
