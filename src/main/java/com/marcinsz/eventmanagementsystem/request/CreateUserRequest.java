package com.marcinsz.eventmanagementsystem.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @Length(min = 5,max = 10)
    private String username;
    @NotBlank
    @Length(min = 5)
    private String password;
    @Past
    @NotNull
    private LocalDate birthDate;
    @Length(min = 9,max = 9)
    @NotBlank
    private String phoneNumber;
    @Length(min = 10,max = 10)
    @NotBlank
    private String accountNumber;
}
