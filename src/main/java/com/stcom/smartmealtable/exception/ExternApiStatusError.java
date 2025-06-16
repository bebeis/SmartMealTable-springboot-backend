package com.stcom.smartmealtable.exception;

public class ExternApiStatusError extends RuntimeException {
    public ExternApiStatusError(String message) {
        super(message);
    }

    public ExternApiStatusError() {
        super();
    }

    public ExternApiStatusError(String message, Throwable cause) {
        super(message, cause);
    }

    public ExternApiStatusError(Throwable cause) {
        super(cause);
    }
}
