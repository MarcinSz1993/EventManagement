package com.marcinsz.eventmanagementsystem.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinEventRequest {
    @Email(message = "Acceptable pattern is: example@example.com")
    @NotBlank(message = "Email is required")
    public String email;
}
