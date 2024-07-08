package com.marcinsz.eventmanagementsystem.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    @Length(min = 5,max = 10)
    @NotBlank
    private String username;

    @Length(min = 5)
    @NotBlank
    private String password;
}
