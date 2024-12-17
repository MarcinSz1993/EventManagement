package com.marcinsz.eventmanagementsystem.exception;

public class BadCredentialsException extends RuntimeException{
    public BadCredentialsException(){
        super("You typed incorrect login or password.");
    }

    public BadCredentialsException(String message){
        super(message);
    }
}
