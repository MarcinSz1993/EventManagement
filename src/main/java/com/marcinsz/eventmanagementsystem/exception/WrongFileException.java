package com.marcinsz.eventmanagementsystem.exception;

public class WrongFileException extends RuntimeException {
    public WrongFileException(String message) {
        super(message);
    }
    public WrongFileException() {
        super("Wrong file!");
    }
}
