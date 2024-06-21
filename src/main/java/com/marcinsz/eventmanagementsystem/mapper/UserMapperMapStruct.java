package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapperMapStruct {
    @Mapping(target = "user_id", source = "id")
    UserDto convertUserToUserDto(User user);

    @Mappings({
            @Mapping(target = "role", constant = "USER"),
            @Mapping(target = "accountStatus", constant = "ACTIVE")
    })
    User convertCreateUserRequestToUser(CreateUserRequest createUserRequest);

    List<UserDto> convertListUserToListUserDto(List<User> userList);

}
