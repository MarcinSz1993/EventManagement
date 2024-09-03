package com.marcinsz.eventmanagementsystem.exception;

public class TicketAlreadyBoughtException extends RuntimeException{
    public TicketAlreadyBoughtException(String message) {
        super(message);
    }
}
