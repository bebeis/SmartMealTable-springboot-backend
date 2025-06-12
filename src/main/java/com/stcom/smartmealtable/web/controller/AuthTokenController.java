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
    public ApiResponse<JwtTokenResponseDto> login(@RequestBody EmailLoginRequest request) {
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
    public ApiResponse<JwtTokenResponseDto> socialLogin(@RequestBody SocialLoginRequest request) {
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
                                                                      @RequestBody RefreshTokenRequest request) {
        String accessToken = jwtTokenService.createAccessToken(memberDto.getMemberId(), memberDto.getProfileId());
        return ApiResponse.createSuccess(new AccessTokenRefreshResponse(accessToken, 3600, "Bearer"));
    }
    
    @Data
    @AllArgsConstructor
    public static class EmailLoginRequest {
        @NotEmpty
        @Email
        private String email;
        @NotEmpty
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class SocialLoginRequest {
        @NotEmpty
        private String provider;
        @NotEmpty
        private String authorizationCode;
    }

    @Data
    public static class RefreshTokenRequest {
        @NotEmpty
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