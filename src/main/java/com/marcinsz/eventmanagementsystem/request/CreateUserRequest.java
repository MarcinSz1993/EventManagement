package com.marcinsz.eventmanagementsystem.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "First name is required")
    private String firstName;
    @NotBlank(message = "Last name is required")
    private String lastName;
    @Email(message = "Acceptable pattern is: example@example.com")
    @NotBlank(message = "Email is required")
    private String email;
    @NotBlank(message = "Username is required")
    @Length(min = 5,max = 10,message = "Username must have between 5 and 10 characters")
    private String username;
    @NotBlank(message = "Password is required")
    @Length(min = 5,message = "Password must have at least 5 characters")
    private String password;
    @Past(message = "A day of birth must be past")
    @NotNull(message = "A day of birth is required")
    private LocalDate birthDate;
    @Length(min = 9,max = 9,message = "Phone number must have exactly 9 numbers")
    @NotBlank(message = "Phone number is required")
    private String phoneNumber;
    @Length(min = 10,max = 10,message = "Account number must have exactly 10 numbers")
    @NotBlank(message = "Account number is required")
    private String accountNumber;
}
