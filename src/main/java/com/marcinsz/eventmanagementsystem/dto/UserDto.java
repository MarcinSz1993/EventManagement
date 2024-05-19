package com.marcinsz.eventmanagementsystem.dto;

import com.marcinsz.eventmanagementsystem.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long user_id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDate birthDate;
    private Role role;
    private String phoneNumber;
    private String accountNumber;
    private String accountStatus;
}
