package com.marcinsz.eventmanagementsystem.exception;

public class EventNotFinishedException extends RuntimeException{
    public EventNotFinishedException(){
        super("Event has been not finished yet");
    }
}
