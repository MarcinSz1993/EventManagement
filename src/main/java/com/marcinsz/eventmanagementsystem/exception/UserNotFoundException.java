package com.marcinsz.eventmanagementsystem.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message){
        super(message);
    }
    public static UserNotFoundException forUsername(String username){
        return new UserNotFoundException("There is no user with username: " + username);
    }

    public static UserNotFoundException forEmail(String email){
        return new UserNotFoundException("There is no user with email: " + email);
    }

}
