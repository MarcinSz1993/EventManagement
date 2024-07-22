package com.marcinsz.eventmanagementsystem.exception;

public class EventNotFoundException extends RuntimeException{
    public EventNotFoundException(Long id){
        super("The event with id: " + id + " does not exist.");
    }
    public EventNotFoundException(String eventName){
        super("The event with event name: " + eventName.toUpperCase() + " does not exist.");
    }
}
