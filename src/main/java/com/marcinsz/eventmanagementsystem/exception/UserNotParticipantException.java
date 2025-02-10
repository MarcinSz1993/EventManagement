package com.marcinsz.eventmanagementsystem.exception;

public class UserNotParticipantException extends RuntimeException {
    public UserNotParticipantException(String username, String eventName) {
        super("User " + username + " is not participant of event " + eventName);
    }

    public UserNotParticipantException(String message) {
        super(message);
    }
}
