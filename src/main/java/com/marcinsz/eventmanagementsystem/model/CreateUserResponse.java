package com.marcinsz.eventmanagementsystem.model;

import com.marcinsz.eventmanagementsystem.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
@Getter
@Builder
@Component
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserResponse {
    private UserDto userDto;
    private String token;
}
