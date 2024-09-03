package com.marcinsz.eventmanagementsystem.exception;

public class TransactionProcessServerException extends RuntimeException{
    public TransactionProcessServerException(String message){
        super(message);
    }
}
