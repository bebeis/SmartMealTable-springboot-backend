package com.stcom.smartmealtable.exception;

public class ExternApiStatusError extends RuntimeException {
    public ExternApiStatusError(String message) {
        super(message);
    }
}
