package com.marcinsz.eventmanagementsystem.exception;

public class InvalidJsonFileException extends RuntimeException{
    public InvalidJsonFileException() {
        super("Invalid JSON file.");
    }
}
