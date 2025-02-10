package com.marcinsz.eventmanagementsystem.controller;

import com.marcinsz.eventmanagementsystem.dto.EventDto;
import com.marcinsz.eventmanagementsystem.model.AuthenticationResponse;
import com.marcinsz.eventmanagementsystem.model.CreateUserResponse;
import com.marcinsz.eventmanagementsystem.model.PageResponse;
import com.marcinsz.eventmanagementsystem.request.AuthenticationRequest;
import com.marcinsz.eventmanagementsystem.request.ChangePasswordRequest;
import com.marcinsz.eventmanagementsystem.request.CreateUserRequest;
import com.marcinsz.eventmanagementsystem.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Validated
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;



    @GetMapping("/email")
    public ResponseEntity<String> getEmailFromToken(@RequestHeader("Authorization")String token) {
        return ResponseEntity.ok(userService.getEmailFromToken(token));
    }


    @PostMapping
    public CreateUserResponse createUser(@RequestBody @Valid CreateUserRequest createUserRequest,
                                         HttpServletResponse servletResponse){
        CreateUserResponse userResponse = userService.createUser(createUserRequest);
        String token = userResponse.getToken();
        addTokenToCookie(token,servletResponse);
        return userResponse;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest authenticationRequest,
                                        HttpServletRequest httpServletRequest,
                                        HttpServletResponse servletResponse) {
        AuthenticationResponse login = userService.login(authenticationRequest,
                                                         httpServletRequest);
        String token = login.getToken();
        addTokenToCookie(token,servletResponse);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(login);
    }

    @GetMapping("/preferences")
    public ResponseEntity<PageResponse<EventDto>> getEventsListBasedOnUserPreferences(@RequestHeader("Authorization") String authorizationHeader,
                                                                                      @RequestParam(name = "page",defaultValue = "0",required = false) int page,
                                                                                      @RequestParam(name = "size",defaultValue = "2",required = false) int size){
        String token = authorizationHeader.substring("Bearer ".length());
        System.out.println(token);
        return ResponseEntity.ok(userService.getEventsBasedOnUserPreferences(token,page,size));
    }

    @PatchMapping("/changePassword")
    public ResponseEntity<String> changePassword(ChangePasswordRequest changePasswordRequest, Principal connectedUser){
        userService.changePassword(changePasswordRequest,connectedUser);
        return ResponseEntity.ok("Password has been changed successfully");
    }

    private void addTokenToCookie(String token, HttpServletResponse servletResponse){
        Cookie cookie = new Cookie("token",token);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(24*60*60);
        cookie.setPath("/");
        servletResponse.addCookie(cookie);
    }
}
