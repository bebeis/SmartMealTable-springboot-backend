package com.stcom.smartmealtable.web.auth;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.security.JwtAuthorization;
import com.stcom.smartmealtable.security.JwtBlacklistService;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.LoginService;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final LoginService loginService;
    private final JwtTokenService jwtTokenService;
    private final JwtBlacklistService jwtBlacklistService;

    @PostMapping("/login")
    public ApiResponse<?> login(@JwtAuthorization Member member, @RequestBody LoginRequest request)
            throws PasswordFailedExceededException {
        if (member == null) {
            member = loginService.login(request.getEmail(), request.getPassword());
        }

        JwtTokenResponseDto tokenResponseDto = jwtTokenService.createTokenDto(member.getId());
        if (member.getMemberProfile() == null) {
            tokenResponseDto.setNewUser(true);
        }
        log.info("response = {}", tokenResponseDto);
        return ApiResponse.createSuccess(tokenResponseDto);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (jwt.isBlank()) {
            return ApiResponse.createError("인증 토큰이 없습니다");
        }
        jwtBlacklistService.addToBlacklist(jwt);
        return ApiResponse.createSuccessWithNoContent();
    }

    @Data
    static class LoginRequest {

        @NotEmpty
        @Email
        private String email;

        @NotEmpty
        private String password;
    }
}
