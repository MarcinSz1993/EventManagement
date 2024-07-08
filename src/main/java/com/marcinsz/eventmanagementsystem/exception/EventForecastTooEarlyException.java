package com.marcinsz.eventmanagementsystem.exception;

public class EventForecastTooEarlyException extends RuntimeException {
    public EventForecastTooEarlyException(String msg){
        super(msg);
    }
}
