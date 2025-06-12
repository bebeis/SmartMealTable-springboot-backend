package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.infrastructure.SocialAuthService;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.security.JwtBlacklistService;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.LoginService;
import com.stcom.smartmealtable.service.dto.AuthResultDto;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 토큰 관련 API (로그인 / 로그아웃 / 토큰 리프레시 / 소셜 로그인).
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthTokenController {

    private final LoginService loginService;
    private final JwtTokenService jwtTokenService;
    private final JwtBlacklistService jwtBlacklistService;
    private final SocialAuthService socialAuthService;

    @PostMapping("/login")
    public ApiResponse<JwtTokenResponseDto> login(@Valid @RequestBody EmailLoginRequest request) {
        AuthResultDto authResultDto = loginService.loginWithEmail(request.getEmail(), request.getPassword());
        JwtTokenResponseDto jwtDto = jwtTokenService.createTokenDto(authResultDto.getMemberId(),
                authResultDto.getProfileId());
        if (authResultDto.isNewUser()) {
            jwtDto.setNewUser(true);
        }
        return ApiResponse.createSuccess(jwtDto);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        String jwt = request.getHeader("Authorization");
        if (Objects.nonNull(jwt)) {
            jwtBlacklistService.addToBlacklist(jwt);
        }
        return ApiResponse.createSuccessWithNoContent();
    }

    @PostMapping("/oauth2/code")
    public ApiResponse<JwtTokenResponseDto> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        TokenDto token = socialAuthService.getTokenResponse(request.getProvider().toLowerCase(),
                request.getAuthorizationCode());
        AuthResultDto authResultDto = loginService.socialLogin(token);
        JwtTokenResponseDto jwtDto = jwtTokenService.createTokenDto(authResultDto.getMemberId(),
                authResultDto.getProfileId());
        if (authResultDto.isNewUser()) {
            jwtDto.setNewUser(true);
        }
        return ApiResponse.createSuccess(jwtDto);
    }

    @PostMapping("/token/refresh")
    public ApiResponse<AccessTokenRefreshResponse> refreshAccessToken(@UserContext MemberDto memberDto,
                                                                      @Valid @RequestBody RefreshTokenRequest request) {
        String accessToken = jwtTokenService.createAccessToken(memberDto.getMemberId(), memberDto.getProfileId());
        return ApiResponse.createSuccess(new AccessTokenRefreshResponse(accessToken, 3600, "Bearer"));
    }
    
    @Data
    @AllArgsConstructor
    public static class EmailLoginRequest {
        @NotEmpty(message = "이메일은 비어있을 수 없습니다")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;
        @NotEmpty(message = "비밀번호는 비어있을 수 없습니다")
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class SocialLoginRequest {
        @NotEmpty(message = "Provider는 비어있을 수 없습니다")
        private String provider;
        @NotEmpty(message = "인증 코드는 비어있을 수 없습니다")
        private String authorizationCode;
    }

    @Data
    public static class RefreshTokenRequest {
        @NotEmpty(message = "리프레시 토큰은 비어있을 수 없습니다")
        private String refreshToken;
    }

    @Data
    @AllArgsConstructor
    public static class AccessTokenRefreshResponse {
        private String accessToken;
        private int expiresIn;
        private String tokenType;
    }
} 