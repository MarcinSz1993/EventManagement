package com.marcinsz.eventmanagementsystem.exception;

public class NotEnoughMoneyException extends RuntimeException {
    public NotEnoughMoneyException() {
        super("Not enough money on your bank account to execute the transaction.");
    }
}
