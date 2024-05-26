package com.marcinsz.eventmanagementsystem.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinEventRequest {
    @NotBlank(message = "First name is required")
    public String firstName;
    @NotBlank(message = "Last name is required")
    public String lastName;
    @Email(message = "Acceptable pattern is: example@example.com")
    @NotBlank(message = "Email is required")
    public String email;
    @Past(message = "A day of birth must be past")
    @NotNull(message = "A day of birth is required")
    public LocalDate birthDate;
}
