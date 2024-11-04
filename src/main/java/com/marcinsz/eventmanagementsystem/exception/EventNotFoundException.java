package com.marcinsz.eventmanagementsystem.exception;

public class EventNotFoundException extends RuntimeException{
    public EventNotFoundException(Long id){
        super(String.format("Event with id %d not found", id));
    }
    public EventNotFoundException(String eventName){
        super(String.format("The event with event name: %s does not exists.",eventName.toUpperCase()));
    }
}
