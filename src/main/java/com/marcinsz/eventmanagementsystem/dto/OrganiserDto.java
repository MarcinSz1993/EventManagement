package com.marcinsz.eventmanagementsystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganiserDto {

    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String phoneNumber;
}
