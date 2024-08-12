package com.marcinsz.eventmanagementsystem.exception;

public class ReviewAlreadyWrittenException extends RuntimeException{
    public ReviewAlreadyWrittenException() {
        super("You have already written a review for this event.");
    }
}
