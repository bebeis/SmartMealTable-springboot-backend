package com.stcom.smartmealtable.web.controller;


import com.stcom.smartmealtable.infrastructure.SocialAuthService;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.LoginService;
import com.stcom.smartmealtable.service.dto.AuthResultDto;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final JwtTokenService jwtTokenService;
    private final SocialAuthService socialAuthService;
    private final LoginService loginService;

    @PostMapping("/oauth2/code")
    public ApiResponse<JwtTokenResponseDto> getTokenFromSocial(@RequestBody JwtTokenRequest request) {
        TokenDto token = socialAuthService.getTokenResponse(
                request.getProvider().toLowerCase(), request.getAuthorizationCode());
        AuthResultDto authResultDto = loginService.socialLogin(token);
        JwtTokenResponseDto jwtDto =
                jwtTokenService.createTokenDto(authResultDto.getMemberId(), authResultDto.getProfileId());
        if (authResultDto.isNewUser()) {
            jwtDto.setNewUser(true);
        }
        return ApiResponse.createSuccess(jwtDto);
    }

    @PostMapping("/token/refresh")
    public ApiResponse<JwtRefreshedAccessTokenDto> refreshAccessToken(@UserContext MemberDto memberDto,
                                                                      @RequestBody JwtRefreshTokenRequest request) {
        String accessToken = jwtTokenService.createAccessToken(memberDto.getMemberId(), memberDto.getProfileId());
        return ApiResponse.createSuccess(
                new JwtRefreshedAccessTokenDto(accessToken, 3600, "Bearar")
        );
    }


    @Data
    @AllArgsConstructor
    static class JwtTokenRequest {

        @NotEmpty
        private String provider;

        @NotEmpty
        private String authorizationCode;
    }

    @Data
    @AllArgsConstructor
    static class JwtRefreshedAccessTokenDto {
        private String accessToken;
        private int expiresIn;
        private String tokenType;
    }

    @Data
    static class JwtRefreshTokenRequest {

        @NotEmpty
        private String refreshToken;
    }

}
