package com.marcinsz.eventmanagementsystem.exception;

public class UserHasNoPreferencesYetException extends RuntimeException {
    public UserHasNoPreferencesYetException() {
        super("User has no preferences");
    }
}
