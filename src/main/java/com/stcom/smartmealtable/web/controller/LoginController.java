package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.security.JwtBlacklistService;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.LoginService;
import com.stcom.smartmealtable.service.dto.AuthResultDto;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
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
    public ApiResponse<?> login(@Validated @RequestBody LoginRequest request)
            throws PasswordFailedExceededException {
        AuthResultDto authResultDto = loginService.loginWithEmail(request.getEmail(), request.getPassword());
        JwtTokenResponseDto jwtTokenResponseDto =
                jwtTokenService.createTokenDto(authResultDto.getMemberId(), authResultDto.getProfileId());
        if (authResultDto.isNewUser()) {
            jwtTokenResponseDto.setNewUser(true);
        }
        return ApiResponse.createSuccess(jwtTokenResponseDto);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
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
