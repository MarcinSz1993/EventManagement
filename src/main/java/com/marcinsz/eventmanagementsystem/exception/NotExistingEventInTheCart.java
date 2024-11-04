package com.marcinsz.eventmanagementsystem.exception;

public class NotExistingEventInTheCart extends RuntimeException{
    public NotExistingEventInTheCart(String eventName) {
        super(String.format("There is no event %s in the cart.", eventName));
    }
}
