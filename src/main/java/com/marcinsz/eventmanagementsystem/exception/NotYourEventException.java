package com.marcinsz.eventmanagementsystem.exception;

public class NotYourEventException extends RuntimeException{
    public NotYourEventException(){
        super("You can update your events only!");
    }
}
