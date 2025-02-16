package com.marcinsz.eventmanagementsystem.request;

import com.marcinsz.eventmanagementsystem.model.EventTarget;
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
    @NotEmpty(message = "Event name cannot be empty!")
    @Max(value = 25,message = "Maximum length is 25 characters!")
    private String eventName;
    @NotBlank(message = "You must describe your event")
    @NotEmpty(message = "Description cannot be empty!")
    @Max(value = 500, message = "Maximum length is 500 characters!")
    private String eventDescription;
    @NotBlank(message = "Location is required")
    @Max(value = 15,message = "Max length is 15 characters!")
    @NotEmpty(message ="Location cannot be empty!")
    private String location;
    @NotNull(message = "Max attendees is required")
    @Positive(message = "A number must be greater than 0")
    private int maxAttendees;
    @FutureOrPresent(message = "A event date cannot be past")
    private LocalDate eventDate;
    @PositiveOrZero(message = "A price must not be below 0")
    private double ticketPrice;
    @NotNull(message = "Event target is required")
    private EventTarget eventTarget;
}
