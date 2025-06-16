package com.stcom.smartmealtable.web.exhandler;

import com.stcom.smartmealtable.exception.BizLogicException;
import com.stcom.smartmealtable.exception.ExternApiStatusError;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class ExControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> processValidationError(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        return ApiResponse.createFail(bindingResult);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<?> processValidationValidatorError(HandlerMethodValidationException exception) {
        return ApiResponse.createError(exception.getMessage());
    }


    @ExceptionHandler(PasswordPolicyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> passwordPolicyExHandler(PasswordPolicyException e) {
        log.error("[PasswordPolicyException] ex", e);
        return ApiResponse.createError(e.getMessage());
    }

    @ExceptionHandler(PasswordFailedExceededException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Object> passwordFailedExceededExHandler(PasswordFailedExceededException e) {
        log.error("[PasswordFailedExceededException] ex", e);
        return ApiResponse.createError(e.getMessage());
    }

    @ExceptionHandler(BizLogicException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> bizLogicExHandler(BizLogicException e) {
        log.error("[BizLogicException] ex", e);
        return ApiResponse.createError("불가능한 시도입니다. 사유: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> illegalArgumentExHandler(IllegalArgumentException e) {
        log.error("[IllegalArgumentException] ex", e);
        return ApiResponse.createError("잘못된 데이터가 전달되었습니다. 사유: " + e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Object> illegalStateExHandler(IllegalArgumentException e) {
        log.error("[IllegalStateException] ex", e);
        return ApiResponse.createError("서버 내부 동작 오류입니다. 사유: " + e.getMessage());
    }

    @ExceptionHandler(ExternApiStatusError.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> externApiStatusErrorHandler(ExternApiStatusError e) {
        log.error("[ExternApiStatusError] ex", e);
        return ApiResponse.createError("외부 API 호출 중 오류가 발생했습니다. 사유: " + e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> runtimeExHandler(Exception e) {
        log.error("[RuntimeException] ex", e);
        return ApiResponse.createError("서버 내부에서 알 수 없는 오류가 발생했습니다. " + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Object> exHandler(Exception e) {
        log.error("[Exception] ex", e);
        return ApiResponse.createError("서버 내부에서 알 수 없는 오류가 발생했습니다. " + e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNoResourceFound() {
        // 오류, 로그 먹어버리기
    }
}
