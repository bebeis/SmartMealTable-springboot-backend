package com.stcom.smartmealtable.web.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private static final String SUCCESS_STATUS = "SUCCESS";
    private static final String FAIL_STATUS = "FAIL";
    private static final String ERROR_STATUS = "ERROR";

    private String status;
    private String message;
    private T data;

    public static <T> ApiResponse<T> createSuccess(T data) {
        return new ApiResponse<>(SUCCESS_STATUS, null, data);
    }

    public static <T> ApiResponse<T> createSuccessWithNoContent() {
        return new ApiResponse<>(SUCCESS_STATUS, null, null);
    }

    // Hibernate Validator에 의해 유효하지 않은 데이터로 인해 API 호출이 거부될때 반환
    public static ApiResponse<Map<String, String>> createFail(BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        List<ObjectError> allErrors = bindingResult.getAllErrors();
        for (ObjectError error : allErrors) {
            if (error instanceof FieldError) {
                errors.put(((FieldError) error).getField(), error.getDefaultMessage());
            } else {
                errors.put(error.getObjectName(), error.getDefaultMessage());
            }
        }
        return new ApiResponse<>(FAIL_STATUS, null, errors);
    }

    // 예외 발생으로 API 호출 실패시 반환
    public static <T> ApiResponse<T> createError(String message) {
        return new ApiResponse<>(ERROR_STATUS, message, null);
    }


}
