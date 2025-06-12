package com.stcom.smartmealtable.web.exhandler;

import static org.assertj.core.api.Assertions.*;

import com.stcom.smartmealtable.exception.BizLogicException;
import com.stcom.smartmealtable.exception.ExternApiStatusError;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

class ExControllerAdviceTest {

    private ExControllerAdvice exControllerAdvice;

    @BeforeEach
    void setUp() {
        exControllerAdvice = new ExControllerAdvice();
    }

    @Test
    @DisplayName("MethodArgumentNotValidException 처리 테스트")
    void processValidationError() {
        // given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "name", "이름은 비어있을 수 없습니다");
        FieldError fieldError2 = new FieldError("object", "email", "유효한 이메일 형식이 아닙니다");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        // when
        ApiResponse<?> response = exControllerAdvice.processValidationError(exception);

        // then
        assertThat(response.getStatus()).isEqualTo("FAIL");
        assertThat(response.getData()).isNotNull();
    }

    @Test
    @DisplayName("HandlerMethodValidationException 처리 테스트")
    void processValidationValidatorError() {
        // given
        HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
        when(exception.getMessage()).thenReturn("Validation failed");

        // when
        ApiResponse<?> response = exControllerAdvice.processValidationValidatorError(exception);

        // then
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getMessage()).isEqualTo("Validation failed");
    }

    @Test
    @DisplayName("PasswordPolicyException 처리 테스트")
    void passwordPolicyExHandler() {
        // given
        PasswordPolicyException exception = new PasswordPolicyException("비밀번호 정책을 위반했습니다");

        // when
        ApiResponse<Object> response = exControllerAdvice.passwordPolicyExHandler(exception);

        // then
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getMessage()).isEqualTo("비밀번호 정책을 위반했습니다");
    }

    @Test
    @DisplayName("PasswordFailedExceededException 처리 테스트")
    void passwordFailedExceededExHandler() {
        // given
        PasswordFailedExceededException exception = new PasswordFailedExceededException("비밀번호 시도 횟수를 초과했습니다");

        // when
        ApiResponse<Object> response = exControllerAdvice.passwordFailedExceededExHandler(exception);

        // then
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getMessage()).isEqualTo("비밀번호 시도 횟수를 초과했습니다");
    }

    @Test
    @DisplayName("BizLogicException 처리 테스트")
    void bizLogicExHandler() {
        // given
        BizLogicException exception = new BizLogicException("비즈니스 로직 오류");

        // when
        ApiResponse<Object> response = exControllerAdvice.bizLogicExHandler(exception);

        // then
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getMessage()).isEqualTo("불가능한 시도입니다. 사유: 비즈니스 로직 오류");
    }

    @Test
    @DisplayName("IllegalArgumentException 처리 테스트")
    void illegalArgumentExHandler() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("잘못된 인수");

        // when
        ApiResponse<Object> response = exControllerAdvice.illegalArgumentExHandler(exception);

        // then
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getMessage()).isEqualTo("잘못된 데이터가 전달되었습니다. 사유: 잘못된 인수");
    }

    @Test
    @DisplayName("IllegalStateException 처리 테스트")
    void illegalStateExHandler() {
        // given
        IllegalArgumentException exception = new IllegalArgumentException("잘못된 상태");

        // when
        ApiResponse<Object> response = exControllerAdvice.illegalStateExHandler(exception);

        // then
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getMessage()).isEqualTo("서버 내부 동작 오류입니다. 사유: 잘못된 상태");
    }

    @Test
    @DisplayName("ExternApiStatusError 처리 테스트")
    void externApiStatusErrorHandler() {
        // given
        ExternApiStatusError exception = new ExternApiStatusError("외부 API 오류");

        // when
        ApiResponse<Object> response = exControllerAdvice.externApiStatusErrorHandler(exception);

        // then
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getMessage()).isEqualTo("외부 API 호출 중 오류가 발생했습니다. 사유: 외부 API 오류");
    }

    @Test
    @DisplayName("RuntimeException 처리 테스트")
    void runtimeExHandler() {
        // given
        RuntimeException exception = new RuntimeException("런타임 오류");

        // when
        ApiResponse<Object> response = exControllerAdvice.runtimeExHandler(exception);

        // then
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getMessage()).contains("서버 내부에서 알 수 없는 오류가 발생했습니다");
    }

    @Test
    @DisplayName("Exception 처리 테스트")
    void exHandler() throws Exception {
        // given
        Exception exception = new Exception("일반 예외");

        // when
        ApiResponse<Object> response = exControllerAdvice.exHandler(exception);

        // then
        assertThat(response.getStatus()).isEqualTo("ERROR");
        assertThat(response.getMessage()).contains("서버 내부에서 알 수 없는 오류가 발생했습니다");
    }

    @Test
    @DisplayName("NoResourceFoundException 처리 테스트")
    void handleNoResourceFound() {
        // given & when & then
        // void 메서드이므로 예외가 발생하지 않는지만 확인
        assertThatCode(() -> exControllerAdvice.handleNoResourceFound()).doesNotThrowAnyException();
    }
} 