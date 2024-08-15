package com.marcinsz.eventmanagementsystem.dto;

import com.marcinsz.eventmanagementsystem.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private LocalDate birthDate;
    private Role role;
    private String phoneNumber;
    private String accountNumber;
    private String accountStatus;
}
