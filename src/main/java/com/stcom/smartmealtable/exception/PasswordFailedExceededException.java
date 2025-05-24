package com.stcom.smartmealtable.exception;

public class PasswordFailedExceededException extends Exception {
    public PasswordFailedExceededException() {
        super("비밀번호 실패 횟수가 5회를 초과하였습니다.");
    }

    public PasswordFailedExceededException(String message) {
        super(message);
    }

    public PasswordFailedExceededException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordFailedExceededException(Throwable cause) {
        super(cause);
    }

    protected PasswordFailedExceededException(String message, Throwable cause, boolean enableSuppression,
                                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
