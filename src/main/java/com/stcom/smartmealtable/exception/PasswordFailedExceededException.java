package com.stcom.smartmealtable.exception;

public class PasswordFailedExceededException extends BizLogicException {
    public PasswordFailedExceededException() {
        super("비밀번호 실패 횟수가 5회를 초과하였습니다.");
    }

    public PasswordFailedExceededException(String message) {
        super(message);
    }
    
}
