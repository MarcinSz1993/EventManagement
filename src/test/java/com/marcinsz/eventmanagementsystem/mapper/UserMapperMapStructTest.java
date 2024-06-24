package com.marcinsz.eventmanagementsystem.mapper;

import com.marcinsz.eventmanagementsystem.dto.OrganiserDto;
import com.marcinsz.eventmanagementsystem.dto.UserDto;
import com.marcinsz.eventmanagementsystem.model.Role;
import com.marcinsz.eventmanagementsystem.model.User;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
@SpringBootTest
@ExtendWith(SpringExtension.class)
class UserMapperMapStructTest {

    @Autowired
    private UserMapperMapStruct userMapperMapStruct;

    @Test
    public void convertUserToUserDto(){
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

        UserDto expectedUserDto = UserDto.builder()
                .user_id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .username(user.getUsername())
                .birthDate(user.getBirthDate())
                .role(user.getRole())
                .phoneNumber(user.getPhoneNumber())
                .accountNumber(user.getAccountNumber())
                .accountStatus(user.getAccountStatus())
                .build();
        UserDto actualUserDto = userMapperMapStruct.convertUserToUserDto(user);

        assertEquals(expectedUserDto.getUser_id(),actualUserDto.getUser_id());
        assertEquals(expectedUserDto.getFirstName(),actualUserDto.getFirstName());
        assertEquals(expectedUserDto.getLastName(),actualUserDto.getLastName());
        assertEquals(expectedUserDto.getEmail(),actualUserDto.getEmail());
        assertEquals(expectedUserDto.getUsername(),actualUserDto.getUsername());
        assertEquals(expectedUserDto.getBirthDate(),actualUserDto.getBirthDate());
        assertEquals(expectedUserDto.getRole(),actualUserDto.getRole());
        assertEquals(expectedUserDto.getPhoneNumber(),actualUserDto.getPhoneNumber());
        assertEquals(expectedUserDto.getAccountNumber(),actualUserDto.getAccountNumber());
        assertEquals(expectedUserDto.getAccountStatus(),actualUserDto.getAccountStatus());

        assertEquals(expectedUserDto,actualUserDto);

    }

    @Test
    public void testConvertListUserToListUserDto() {
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

        List<UserDto> userDtoList = userMapperMapStruct.convertListUserToListUserDto(userList);

        assertEquals(userList.size(), userDtoList.size());

        UserDto userDto1 = userDtoList.get(0);
        assertEquals(user1.getId(), userDto1.getUser_id());
        assertEquals(user1.getFirstName(), userDto1.getFirstName());
        assertEquals(user1.getLastName(), userDto1.getLastName());
        assertEquals(user1.getEmail(), userDto1.getEmail());
        assertEquals(user1.getUsername(), userDto1.getUsername());
        assertEquals(user1.getBirthDate(), userDto1.getBirthDate());
        assertEquals(user1.getRole(), userDto1.getRole());
        assertEquals(user1.getPhoneNumber(), userDto1.getPhoneNumber());

        UserDto userDto2 = userDtoList.get(1);
        assertEquals(user2.getId(), userDto2.getUser_id());
        assertEquals(user2.getFirstName(), userDto2.getFirstName());
        assertEquals(user2.getLastName(), userDto2.getLastName());
        assertEquals(user2.getEmail(), userDto2.getEmail());
        assertEquals(user2.getUsername(), userDto2.getUsername());
        assertEquals(user2.getBirthDate(), userDto2.getBirthDate());
        assertEquals(user2.getRole(), userDto2.getRole());
        assertEquals(user2.getPhoneNumber(), userDto2.getPhoneNumber());

    }
    @Test
    public void convertUserToOrganiserDto(){
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

        OrganiserDto expectedOrganiserDto = OrganiserDto.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();

        OrganiserDto actualOrganiserDto = userMapperMapStruct.convertUserToOrganiserDto(user);

        assertEquals(expectedOrganiserDto.getFirstName(),actualOrganiserDto.getFirstName());
        assertEquals(expectedOrganiserDto.getLastName(),actualOrganiserDto.getLastName());
        assertEquals(expectedOrganiserDto.getUserName(),actualOrganiserDto.getUserName());
        assertEquals(expectedOrganiserDto.getEmail(),actualOrganiserDto.getEmail());
        assertEquals(expectedOrganiserDto.getPhoneNumber(),actualOrganiserDto.getPhoneNumber());
    }

    @Test
    public void convertCreateUserRequestToUser(){
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

        User expectedUser = User.builder()
                .id(null)
                .firstName(createUserRequest.getFirstName())
                .lastName(createUserRequest.getLastName())
                .email(createUserRequest.getEmail())
                .username(createUserRequest.getUsername())
                .password(createUserRequest.getPassword())
                .birthDate(createUserRequest.getBirthDate())
                .role(Role.USER)
                .phoneNumber(createUserRequest.getPhoneNumber())
                .accountNumber(createUserRequest.getAccountNumber())
                .accountStatus("ACTIVE")
                .events(null)
                .organizedEvents(null)
                .build();

        User actualUser = userMapperMapStruct.convertCreateUserRequestToUser(createUserRequest);

        assertEquals(expectedUser.getId(),actualUser.getId());
        assertEquals(expectedUser.getFirstName(),actualUser.getFirstName());
        assertEquals(expectedUser.getLastName(),actualUser.getLastName());
        assertEquals(expectedUser.getEmail(),actualUser.getEmail());
        assertEquals(expectedUser.getUsername(),actualUser.getUsername());
        assertEquals(expectedUser.getPassword(),actualUser.getPassword());
        assertEquals(expectedUser.getBirthDate(),actualUser.getBirthDate());
        assertEquals(expectedUser.getRole(),actualUser.getRole());
        assertEquals(expectedUser.getPhoneNumber(),actualUser.getPhoneNumber());
        assertEquals(expectedUser.getAccountNumber(),actualUser.getAccountNumber());
        assertEquals(expectedUser.getAccountStatus(),actualUser.getAccountStatus());
        assertEquals(expectedUser.getEvents(),actualUser.getEvents());
        assertEquals(expectedUser.getOrganizedEvents(),actualUser.getOrganizedEvents());
    }

}