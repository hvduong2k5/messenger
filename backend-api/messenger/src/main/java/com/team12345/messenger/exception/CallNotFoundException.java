package com.team12345.messenger.exception;

public class CallNotFoundException extends RuntimeException {
    public CallNotFoundException(String message) {
        super(message);
    }
}
