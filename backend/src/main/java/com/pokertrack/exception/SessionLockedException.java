package com.pokertrack.exception;

public class SessionLockedException extends RuntimeException {
    public SessionLockedException() {
        super("Session is locked. Financial data cannot be modified.");
    }

    public SessionLockedException(String message) {
        super(message);
    }
}
