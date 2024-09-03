package com.marcinsz.eventmanagementsystem.exception;

public class BankServiceServerNotAvailableException extends RuntimeException {
    public BankServiceServerNotAvailableException(){
        super("Bank Service server is not available");
    }
}
