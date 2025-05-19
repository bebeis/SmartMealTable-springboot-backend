package com.stcom.smartmealtable.web.auth;


import com.stcom.smartmealtable.infrastructure.social.SocialAuthService;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.SocialAccountService;
import com.stcom.smartmealtable.service.dto.token.JwtTokenResponseDto;
import com.stcom.smartmealtable.service.dto.token.TokenDto;
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

    private final SocialAuthService socialManager;
    private final SocialAccountService socialAccountService;
    private final JwtTokenService jwtTokenService;
    private final SocialAuthService socialAuthService;

    @PostMapping("/oauth2/code")
    public ApiResponse<JwtTokenResponseDto> getTokenFromSocial(@RequestBody JwtTokenRequest request) {
        log.info("request = {}", request);
        TokenDto token = socialAuthService.getTokenResponse(request.getProvider(), request.getAuthorizationCode());
        log.info("response 성공 = {}", token);
        boolean isNewUser = socialAccountService.isNewUser(token.getProvider(),
                token.getProviderUserId());
        log.info("새로운 멤버인지 확인 = {}", isNewUser);
        if (isNewUser) {
            socialAccountService.createNewAccount(token);
        }

        JwtTokenResponseDto tokenDto = jwtTokenService.createTokenDto(
                socialAccountService.findMemberId(token.getProvider(), token.getProviderUserId()));
        tokenDto.setNewUser(isNewUser);
        log.info("response = {}", tokenDto);
        return ApiResponse.createSuccess(tokenDto);
    }

    @PostMapping("/api/v1/auth/token/refresh")
    public ApiResponse<JwtRefreshedAccessTokenDto> refreshAccessToken(@RequestBody JwtRefreshTokenRequest request) {
        String memberId = jwtTokenService.extractMemberIdFromRefreshToken(request.getRefreshToken());
        String accessToken = jwtTokenService.createAccessToken(memberId);

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
