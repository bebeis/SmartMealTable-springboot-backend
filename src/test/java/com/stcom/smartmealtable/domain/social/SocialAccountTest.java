package com.stcom.smartmealtable.domain.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stcom.smartmealtable.domain.member.Member;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SocialAccountTest {

    @Test
    @DisplayName("SocialAccount 객체가 빌더를 통해 올바르게 생성된다")
    void createSocialAccountWithBuilder() {
        // given
        Member member = mock(Member.class);
        String provider = "KAKAO";
        String providerUserId = "12345";
        String tokenType = "Bearer";
        String accessToken = "access-token-value";
        String refreshToken = "refresh-token-value";
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(1);
        
        // when
        SocialAccount socialAccount = SocialAccount.builder()
                .member(member)
                .provider(provider)
                .providerUserId(providerUserId)
                .tokenType(tokenType)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenExpiresAt(expiresAt)
                .build();
        
        // then
        assertThat(socialAccount.getMember()).isEqualTo(member);
        assertThat(socialAccount.getProvider()).isEqualTo(provider);
        assertThat(socialAccount.getProviderUserId()).isEqualTo(providerUserId);
        assertThat(socialAccount.getTokenType()).isEqualTo(tokenType);
        assertThat(socialAccount.getAccessToken()).isEqualTo(accessToken);
        assertThat(socialAccount.getRefreshToken()).isEqualTo(refreshToken);
        assertThat(socialAccount.getTokenExpiresAt()).isEqualTo(expiresAt);
    }
    
    @Test
    @DisplayName("토큰 정보를 업데이트할 수 있다")
    void updateTokenInformation() {
        // given
        Member member = mock(Member.class);
        SocialAccount socialAccount = SocialAccount.builder()
                .member(member)
                .provider("KAKAO")
                .providerUserId("12345")
                .tokenType("Bearer")
                .accessToken("old-access-token")
                .refreshToken("old-refresh-token")
                .tokenExpiresAt(LocalDateTime.now())
                .build();
                
        String newAccessToken = "new-access-token";
        String newRefreshToken = "new-refresh-token";
        LocalDateTime newExpiresAt = LocalDateTime.now().plusHours(2);
        
        // when
        socialAccount.updateToken(newAccessToken, newRefreshToken, newExpiresAt);
        
        // then
        assertThat(socialAccount.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(socialAccount.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(socialAccount.getTokenExpiresAt()).isEqualTo(newExpiresAt);
    }
    
    @Test
    @DisplayName("회원 프로필 등록 여부를 확인할 수 있다")
    void checkIfProfileIsRegistered() {
        // given
        Member memberWithProfile = mock(Member.class);
        when(memberWithProfile.isProfileRegistered()).thenReturn(true);
        
        Member memberWithoutProfile = mock(Member.class);
        when(memberWithoutProfile.isProfileRegistered()).thenReturn(false);
        
        SocialAccount accountWithProfile = SocialAccount.builder()
                .member(memberWithProfile)
                .provider("KAKAO")
                .providerUserId("12345")
                .build();
                
        SocialAccount accountWithoutProfile = SocialAccount.builder()
                .member(memberWithoutProfile)
                .provider("GOOGLE")
                .providerUserId("67890")
                .build();
        
        // when & then
        assertThat(accountWithProfile.isProfileRegistered()).isTrue();
        assertThat(accountWithoutProfile.isProfileRegistered()).isFalse();
    }
    
    @Test
    @DisplayName("다양한 소셜 제공자로 계정을 생성할 수 있다")
    void createAccountsWithDifferentProviders() {
        // given
        Member member = mock(Member.class);
        
        // when
        SocialAccount kakaoAccount = SocialAccount.builder()
                .member(member)
                .provider("KAKAO")
                .providerUserId("kakao-12345")
                .build();
                
        SocialAccount googleAccount = SocialAccount.builder()
                .member(member)
                .provider("GOOGLE")
                .providerUserId("google-12345")
                .build();
                
        SocialAccount naverAccount = SocialAccount.builder()
                .member(member)
                .provider("NAVER")
                .providerUserId("naver-12345")
                .build();
        
        // then
        assertThat(kakaoAccount.getProvider()).isEqualTo("KAKAO");
        assertThat(googleAccount.getProvider()).isEqualTo("GOOGLE");
        assertThat(naverAccount.getProvider()).isEqualTo("NAVER");
        
        assertThat(kakaoAccount.getProviderUserId()).isEqualTo("kakao-12345");
        assertThat(googleAccount.getProviderUserId()).isEqualTo("google-12345");
        assertThat(naverAccount.getProviderUserId()).isEqualTo("naver-12345");
    }
} 