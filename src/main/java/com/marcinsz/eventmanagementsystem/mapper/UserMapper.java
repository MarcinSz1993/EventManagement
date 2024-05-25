package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.OrganiserDto;
import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.model.Role;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserMapper {

    public static UserDto convertUserToUserDto(User user){
        return new UserDto(user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUsername(),
                user.getBirthDate(),
                user.getRole(),
                user.getPhoneNumber(),
                user.getAccountNumber(),
                user.getAccountStatus());
    }

    public static User convertCreateUserRequestToUser(CreateUserRequest createUserRequest){
        return new User(createUserRequest.getFirstName(),
                createUserRequest.getLastName(),
                createUserRequest.getEmail(),
                createUserRequest.getUsername(),
                createUserRequest.getPassword(),
                createUserRequest.getBirthDate(),
                Role.USER,
                createUserRequest.getPhoneNumber(),
                createUserRequest.getAccountNumber(),
                "ACTIVE");
    }

    public static OrganiserDto convertUserToOrganiserDto(User user){
        return new OrganiserDto(user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber()

        );
    }
}
