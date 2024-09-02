package com.marcinsz.eventmanagementsystem.exception;

public class TicketAlreadyBought extends RuntimeException{
    public TicketAlreadyBought(String message) {
        super(message);
    }
}
