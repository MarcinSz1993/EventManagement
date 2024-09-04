package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.AuthenticationResponse;
import com.marcinsz.eventmanagementsystem.model.CreateUserResponse;
import com.marcinsz.eventmanagementsystem.request.AuthenticationRequest;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import com.marcinsz.eventmanagementsystem.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("users/")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping
    public CreateUserResponse createUser(@RequestBody @Valid CreateUserRequest createUserRequest,
                                         HttpServletResponse servletResponse){
        CreateUserResponse userResponse = userService.createUser(createUserRequest);
        String token = userResponse.getToken();
        addTokenToCookie(token,servletResponse);
        return userResponse;
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@RequestBody @Valid AuthenticationRequest authenticationRequest,
                                        HttpServletResponse servletResponse) {
        AuthenticationResponse login = userService.login(authenticationRequest);
        String token = login.getToken();
        addTokenToCookie(token,servletResponse);
        return login;
    }

    @GetMapping("/preferences")
    public List<EventDto> getEventsListBasedOnUserPreferences(@RequestHeader("Authorization") String authorizationHeader){
        String token = authorizationHeader.substring("Bearer ".length());
        System.out.println(token);
        return userService.getEventsBasedOnUserPreferences(token);
    }

    private void addTokenToCookie(String token, HttpServletResponse servletResponse){
        Cookie cookie = new Cookie("token",token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(24*60*60);
        cookie.setPath("/");
        servletResponse.addCookie(cookie);
    }
}
