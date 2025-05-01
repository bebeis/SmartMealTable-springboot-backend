package com.stcom.smartmealtable.domain.member;

public class PasswordPolicyException extends Exception {

    public PasswordPolicyException() {
        super();
    }

    public PasswordPolicyException(String message) {
        super(message);
    }

    public PasswordPolicyException(String message, Throwable cause) {
        super(message, cause);
    }

    public PasswordPolicyException(Throwable cause) {
        super(cause);
    }

    protected PasswordPolicyException(String message, Throwable cause, boolean enableSuppression,
                                      boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
