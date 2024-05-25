package com.marcinsz.eventmanagementsystem.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinEventRequest {
    public String firstName;
    public String lastName;
    public String email;
    public LocalDate birthDate;
}
