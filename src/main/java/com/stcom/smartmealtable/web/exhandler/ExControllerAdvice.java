package com.stcom.smartmealtable.web.exhandler;

import com.stcom.smartmealtable.exception.ExternApiStatusError;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExControllerAdvice {

    @ExceptionHandler(PasswordPolicyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> passwordPolicyExHandler(PasswordPolicyException e) {
        log.error("[PasswordPolicyException] ex", e);
        return ApiResponse.createError(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> illegalArgumentExHandler(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] ex", e);
        return ApiResponse.createError("파라미터나 API 스펙을 확인해보세요" + e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> illegalStateExHandler(IllegalArgumentException e) {
        log.error("[IllegalStateException] ex", e);
        return ApiResponse.createError("파라미터나 API 스펙을 확인해보세요" + e.getMessage());
    }

    @ExceptionHandler(PasswordFailedExceededException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Object> passwordFailedExceededExHandler(PasswordFailedExceededException e) {
        log.error("[PasswordFailedExceededException] ex", e);
        return ApiResponse.createError(e.getMessage());
    }

    @ExceptionHandler(ExternApiStatusError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> externApiStatusErrorHandler(ExternApiStatusError e) {
        log.error("[ExternApiStatusError] ex", e);
        return ApiResponse.createError("외부 API 호출 중 오류가 발생했습니다: " + e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> runtimeExHandler(Exception e) {
        log.error("[RuntimeException] ex", e);
        return ApiResponse.createError("서버 내부에서 언체크 예외가 발생했습니다");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> exHandler(Exception e) {
        log.error("[Exception] ex", e);
        return ApiResponse.createError("서버 내부에서 체크 예외가 발생했습니다");
    }
}
