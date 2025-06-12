package com.stcom.smartmealtable.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
// ... existing code ...

import com.stcom.smartmealtable.exception.ExternApiStatusError;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.infrastructure.social.GoogleHttpMessage;
import com.stcom.smartmealtable.infrastructure.social.KakaoHttpMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class SocialAuthServiceTest {

    @Mock
    private KakaoHttpMessage kakaoHttpMessage;

    @Mock
    private GoogleHttpMessage googleHttpMessage;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private SocialAuthService socialAuthService;

    @BeforeEach
    void setUp() {
        socialAuthService.init();
    }

    @DisplayName("정상적으로 토큰을 반환한다")
    @Test
    void getTokenResponse_success() {
        // given
        TokenDto expected = TokenDto.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .expiresIn(3600)
                .provider("kakao")
                .build();

        when(kakaoHttpMessage.getRequestMessage(any(RestClient.class), eq("authCode")))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(kakaoHttpMessage.getTokenResponse(responseSpec)).thenReturn(expected);

        // when
        TokenDto actual = socialAuthService.getTokenResponse("kakao", "authCode");

        // then
        assertEquals(expected, actual);
    }

    @DisplayName("외부 API 예외 발생 시 ExternApiStatusError를 던진다")
    @Test
    void getTokenResponse_fail() {
        // given
        when(kakaoHttpMessage.getRequestMessage(any(RestClient.class), eq("authCode")))
                .thenThrow(new RuntimeException("api error"));

        // when & then
        assertThrows(ExternApiStatusError.class,
                () -> socialAuthService.getTokenResponse("kakao", "authCode"));
    }
} 