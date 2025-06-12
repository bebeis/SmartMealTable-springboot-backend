package com.stcom.smartmealtable.web.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stcom.smartmealtable.infrastructure.SocialAuthService;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.security.JwtBlacklistService;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.LoginService;
import com.stcom.smartmealtable.service.dto.AuthResultDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class AuthTokenControllerTest extends ControllerTestSupport {

    private LoginService loginService;
    private JwtTokenService jwtTokenService;
    private JwtBlacklistService jwtBlacklistService;
    private SocialAuthService socialAuthService;
    private final ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void init() {
        loginService = Mockito.mock(LoginService.class);
        jwtTokenService = Mockito.mock(JwtTokenService.class);
        jwtBlacklistService = Mockito.mock(JwtBlacklistService.class);
        socialAuthService = Mockito.mock(SocialAuthService.class);
        
        AuthTokenController controller = new AuthTokenController(
                loginService, jwtTokenService, jwtBlacklistService, socialAuthService);
        super.setUp(controller);
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 이메일 로그인")
    void login() throws Exception {
        // given
        AuthResultDto authResult = new AuthResultDto(1L, 1L, false);
        JwtTokenResponseDto tokenDto = new JwtTokenResponseDto("accessToken", "refreshToken", 3600, "Bearer");
        
        when(loginService.loginWithEmail(anyString(), anyString())).thenReturn(authResult);
        when(jwtTokenService.createTokenDto(anyLong(), anyLong())).thenReturn(tokenDto);

        String requestBody = om.writeValueAsString(
                new AuthTokenController.EmailLoginRequest("test@example.com", "password123"));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 신규 사용자 로그인")
    void login_newUser() throws Exception {
        // given
        AuthResultDto authResult = new AuthResultDto(1L, 1L, true);
        JwtTokenResponseDto tokenDto = new JwtTokenResponseDto("accessToken", "refreshToken", 3600, "Bearer");
        
        when(loginService.loginWithEmail(anyString(), anyString())).thenReturn(authResult);
        when(jwtTokenService.createTokenDto(anyLong(), anyLong())).thenReturn(tokenDto);

        String requestBody = om.writeValueAsString(
                new AuthTokenController.EmailLoginRequest("newuser@example.com", "password123"));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.newUser").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - 로그아웃")
    void logout() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(jwtBlacklistService).addToBlacklist("Bearer token123");
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - Authorization 헤더 없이 로그아웃")
    void logout_withoutAuthHeader() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(jwtBlacklistService, never()).addToBlacklist(any());
    }

    @Test
    @DisplayName("POST /api/v1/auth/oauth2/code - 소셜 로그인")
    void socialLogin() throws Exception {
        // given
        TokenDto tokenDto = TokenDto.builder()
                .accessToken("socialAccessToken")
                .tokenType("Bearer")
                .expiresIn(3600)
                .provider("google")
                .build();
        AuthResultDto authResult = new AuthResultDto(2L, 2L, false);
        JwtTokenResponseDto jwtTokenDto = new JwtTokenResponseDto("jwtAccessToken", "jwtRefreshToken", 3600, "Bearer");
        
        when(socialAuthService.getTokenResponse(eq("google"), anyString())).thenReturn(tokenDto);
        when(loginService.socialLogin(any(TokenDto.class))).thenReturn(authResult);
        when(jwtTokenService.createTokenDto(anyLong(), anyLong())).thenReturn(jwtTokenDto);

        String requestBody = om.writeValueAsString(
                new AuthTokenController.SocialLoginRequest("google", "authCode123"));

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth2/code")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("jwtAccessToken"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/token/refresh - 액세스 토큰 갱신")
    void refreshAccessToken() throws Exception {
        // given
        when(jwtTokenService.createAccessToken(anyLong(), anyLong())).thenReturn("newAccessToken");

        AuthTokenController.RefreshTokenRequest request = new AuthTokenController.RefreshTokenRequest();
        // RefreshTokenRequest 필드 설정을 위해 reflection 사용하거나 JSON 문자열 사용
        String requestBody = "{\"refreshToken\":\"refreshToken123\"}";

        // when & then
        mockMvc.perform(post("/api/v1/auth/token/refresh")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 유효하지 않은 이메일 형식")
    void login_invalidEmail() throws Exception {
        // given
        String requestBody = "{\"email\":\"invalid-email\", \"password\":\"password123\"}";

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 빈 비밀번호")
    void login_emptyPassword() throws Exception {
        // given
        String requestBody = "{\"email\":\"test@example.com\", \"password\":\"\"}";

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/auth/oauth2/code - 빈 provider")
    void socialLogin_emptyProvider() throws Exception {
        // given
        String requestBody = "{\"provider\":\"\", \"authorizationCode\":\"code123\"}";

        // when & then
        mockMvc.perform(post("/api/v1/auth/oauth2/code")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
} 