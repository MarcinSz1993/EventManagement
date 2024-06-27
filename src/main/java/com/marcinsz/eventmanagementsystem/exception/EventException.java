package com.marcinsz.eventmanagementsystem.exception;

public class EventException extends RuntimeException {
    public EventException(){
        super("You can check forecast 14 days before the event.");
    }
}
