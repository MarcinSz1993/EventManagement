package com.marcinsz.eventmanagementsystem.exception;

public class EventNotFoundException extends RuntimeException{
    public EventNotFoundException(Long id){
        super("The event with id: " + id + " does not exist.");
    }

    public EventNotFoundException(String username){
        super("The event with username: " + username.toUpperCase() + " does not exist.");
    }
}
