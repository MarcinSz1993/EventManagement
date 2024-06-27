package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.OrganiserDto;
import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.model.Role;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Data
public class UserMapper {

    public UserDto convertUserToUserDto(User user) {
        if (user == null) {
            throw new NullPointerException("User should not be null.");
        }
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getBirthDate(),
                user.getRole(),
                user.getPhoneNumber(),
                user.getAccountNumber(),
                user.getAccountStatus()
        );
    }

    public User convertCreateUserRequestToUser(CreateUserRequest createUserRequest) {
        if (createUserRequest == null) {
            throw new NullPointerException("CreateUserRequest should not be null.");
        }
        return new User(
                createUserRequest.getFirstName(),
                createUserRequest.getLastName(),
                createUserRequest.getEmail(),
                createUserRequest.getUsername(),
                createUserRequest.getPassword(),
                createUserRequest.getBirthDate(),
                Role.USER,
                createUserRequest.getPhoneNumber(),
                createUserRequest.getAccountNumber(),
                "ACTIVE"
        );
    }

    public OrganiserDto convertUserToOrganiserDto(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User should not be null.");
        }
        return new OrganiserDto(
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber()
        );
    }

    public List<UserDto> convertListUserToListUserDto(List<User> userList) {
        if (userList == null) {
            return Collections.emptyList();
        }
        return userList.stream()
                .map(user -> new UserDto(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getUsername(),
                        user.getBirthDate(),
                        user.getRole(),
                        user.getPhoneNumber(),
                        user.getAccountNumber(),
                        user.getAccountStatus()
                ))
                .collect(Collectors.toList());
    }
}
