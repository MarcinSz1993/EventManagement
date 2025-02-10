package com.marcinsz.eventmanagementsystem.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationRequest {
    @Length(min = 5,max = 15,message = "Username should have between 5 and 15 characters.")
    @NotBlank(message = "Username is required!")
    private String username;

    @Length(min = 5,max = 25,message = "Password should have between 5 and 25 characters.")
    @NotBlank(message = "Password is required!")
    private String password;
}
