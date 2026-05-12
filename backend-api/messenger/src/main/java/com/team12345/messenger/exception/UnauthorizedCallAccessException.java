package com.team12345.messenger.exception;

public class UnauthorizedCallAccessException extends RuntimeException {
    public UnauthorizedCallAccessException(String message) {
        super(message);
    }
}
