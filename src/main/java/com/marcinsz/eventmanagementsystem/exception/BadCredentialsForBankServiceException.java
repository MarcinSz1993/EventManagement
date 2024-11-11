package com.marcinsz.eventmanagementsystem.exception;

public class BadCredentialsForBankServiceException extends RuntimeException{
    public BadCredentialsForBankServiceException() {
        super("You typed incorrect bank account number or password!");
    }
}
