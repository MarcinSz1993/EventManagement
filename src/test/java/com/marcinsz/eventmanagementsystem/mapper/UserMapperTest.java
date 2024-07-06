package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.OrganiserDto;
import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.model.Role;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class UserMapperTest {
    @Test
    public void shouldMapUserToUserDto(){
        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993,4,20))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

        UserDto userDto = UserMapper.convertUserToUserDto(user);

        Assertions.assertEquals(user.getId(),userDto.getUserId());
        Assertions.assertEquals(user.getFirstName(),userDto.getFirstName());
        Assertions.assertEquals(user.getLastName(),userDto.getLastName());
        Assertions.assertEquals(user.getEmail(),userDto.getEmail());
        Assertions.assertEquals(user.getUsername(),userDto.getUsername());
        Assertions.assertEquals(user.getBirthDate(),userDto.getBirthDate());
        Assertions.assertEquals(user.getRole(),userDto.getRole());
        Assertions.assertEquals(user.getPhoneNumber(),userDto.getPhoneNumber());
        Assertions.assertEquals(user.getAccountNumber(),userDto.getAccountNumber());
        Assertions.assertEquals(user.getAccountStatus(),userDto.getAccountStatus());
    }
    @Test
    public void mapUserToUserDtoShouldThrowNullPointerExceptionWhenInputIsNull(){
        NullPointerException exception = assertThrows(NullPointerException.class, () -> UserMapper.convertUserToUserDto(null));
        assertEquals("User should not be null.", exception.getMessage());
    }

    @Test
    public void shouldMapCreateUserRequestToUser(){
        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993,4,19))
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .build();

        User user = UserMapper.convertCreateUserRequestToUser(createUserRequest);

        assertEquals(createUserRequest.getFirstName(),user.getFirstName());
    }

    @Test
    public void mapCreateUserRequestToUserShouldThrowNullPointerExceptionWhenInputIsNull(){
        NullPointerException exception = assertThrows(NullPointerException.class, () -> UserMapper.convertCreateUserRequestToUser(null));
        assertEquals("CreateUserRequest should not be null.",exception.getMessage());
    }
    @Test
    public void shouldMapUserToOrganiserDto(){

        User user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993,4,20))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

        OrganiserDto organiserDto = UserMapper.convertUserToOrganiserDto(user);

        assertEquals(user.getFirstName(),organiserDto.getFirstName());
        assertEquals(user.getLastName(),organiserDto.getLastName());
        assertEquals(user.getUsername(),organiserDto.getUserName());
        assertEquals(user.getEmail(),organiserDto.getEmail());
        assertEquals(user.getPhoneNumber(),organiserDto.getPhoneNumber());
    }

    @Test
    public void mapUserToOrganiserDtoShouldThrowNullPointerExceptionWhenInputIsNull(){
        NullPointerException exception = assertThrows(NullPointerException.class, () -> UserMapper.convertUserToUserDto(null));
        assertEquals("User should not be null.", exception.getMessage());
    }

    @Test
    public void shouldMapListUserToListUserDtoWithSingleElement() {
        User user1 = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993, 4, 20))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();
        List<User> userList = new ArrayList<>();
        userList.add(user1);
        List<UserDto> userDtoList = UserMapper.convertListUserToListUserDto(userList);

        assertEquals(userList.size(),userDtoList.size());
        assertEquals(userList.get(0).getId(),userDtoList.get(0).getUserId());
        assertEquals(userList.get(0).getFirstName(),userDtoList.get(0).getFirstName());
        assertEquals(userList.get(0).getLastName(),userDtoList.get(0).getLastName());
        assertEquals(userList.get(0).getUsername(),userDtoList.get(0).getUsername());
        assertEquals(userList.get(0).getEmail(),userDtoList.get(0).getEmail());
        assertEquals(userList.get(0).getBirthDate(),userDtoList.get(0).getBirthDate());
        assertEquals(userList.get(0).getRole(),userDtoList.get(0).getRole());
        assertEquals(userList.get(0).getPhoneNumber(),userDtoList.get(0).getPhoneNumber());
        assertEquals(userList.get(0).getAccountNumber(),userDtoList.get(0).getAccountNumber());
        assertEquals(userList.get(0).getAccountStatus(),userDtoList.get(0).getAccountStatus());

    }
    @Test
    public void shouldMapListUserToListUserDtoWithMultipleElements(){
        User user1 = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Smith")
                .email("john@smith.com")
                .username("johnny")
                .password("qwerty")
                .birthDate(LocalDate.of(1993,4,20))
                .role(Role.USER)
                .phoneNumber("123456789")
                .accountNumber("1234567890")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();
        User user2 = User.builder()
                .id(2L)
                .firstName("Kate")
                .lastName("Anderson")
                .email("kate@anderson.com")
                .username("katty")
                .password("qwerty")
                .birthDate(LocalDate.of(1996,1,10))
                .role(Role.USER)
                .phoneNumber("987654321")
                .accountNumber("0987654321")
                .accountStatus("ACTIVE")
                .events(Collections.emptyList())
                .organizedEvents(Collections.emptyList())
                .build();

        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);

        List<UserDto> userDtoList = UserMapper.convertListUserToListUserDto(userList);

        assertEquals(userList.size(),userDtoList.size());

        assertEquals(userList.get(0).getId(),userDtoList.get(0).getUserId());
        assertEquals(userList.get(0).getFirstName(),userDtoList.get(0).getFirstName());
        assertEquals(userList.get(0).getLastName(),userDtoList.get(0).getLastName());
        assertEquals(userList.get(0).getUsername(),userDtoList.get(0).getUsername());
        assertEquals(userList.get(0).getEmail(),userDtoList.get(0).getEmail());
        assertEquals(userList.get(0).getBirthDate(),userDtoList.get(0).getBirthDate());
        assertEquals(userList.get(0).getRole(),userDtoList.get(0).getRole());
        assertEquals(userList.get(0).getPhoneNumber(),userDtoList.get(0).getPhoneNumber());
        assertEquals(userList.get(0).getAccountNumber(),userDtoList.get(0).getAccountNumber());
        assertEquals(userList.get(0).getAccountStatus(),userDtoList.get(0).getAccountStatus());

    }
    @Test
    public void mapListUserToListUserDtoShouldReturnEmptyListWhenInputIsNull(){
        List<UserDto> userDtoList = UserMapper.convertListUserToListUserDto(null);
        Assertions.assertTrue(userDtoList.isEmpty());
    }
    @Test
    public void mapListUserToListUserDtoShouldReturnEmptyListWhenInputIsEmptyList(){
        List<User> userList = Collections.emptyList();
        List<UserDto> userDtoList = UserMapper.convertListUserToListUserDto(userList);
        assertTrue(userDtoList.isEmpty());
    }


}