package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import com.stcom.smartmealtable.service.dto.AuthResultDto;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoginServiceAdditionalIntegrationTest {

    @Autowired
    private LoginService loginService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("login_additional@example.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member);
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시도하면 예외가 발생한다")
    void loginWithNonExistentEmail() {
        // given
        String nonExistentEmail = "nonexistent@example.com";
        String password = "password123!";

        // when & then
        assertThatThrownBy(() -> loginService.loginWithEmail(nonExistentEmail, password))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시도하면 예외가 발생한다")
    void loginWithIncorrectPassword() {
        // given
        String incorrectPassword = "wrongPassword123!";

        // when & then
        assertThatThrownBy(() -> loginService.loginWithEmail(member.getEmail(), incorrectPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("프로필이 등록된 회원이 로그인하면 newUser 플래그가 false이다")
    void loginWithRegisteredProfile() throws Exception {
        // given
        MemberProfile profile = MemberProfile.builder()
                .member(member)
                .nickName("테스터")
                .type(MemberType.STUDENT)
                .build();
        memberProfileRepository.save(profile);

        // when
        AuthResultDto result = loginService.loginWithEmail(member.getEmail(), "password123!");

        // then
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.getProfileId()).isEqualTo(profile.getId());
    }

    @Test
    @DisplayName("기존 소셜계정으로 다시 로그인하면 토큰이 갱신된다")
    void socialLoginWithExistingSocialAccount() {
        // given
        // 기존 소셜 계정 생성
        SocialAccount existingAccount = SocialAccount.builder()
                .member(member)
                .provider("google")
                .providerUserId("google123")
                .accessToken("old-token")
                .refreshToken("old-refresh")
                .tokenType("Bearer")
                .tokenExpiresAt(LocalDateTime.now().plusHours(1))
                .build();
        socialAccountRepository.save(existingAccount);

        // 프로필 생성
        MemberProfile profile = MemberProfile.builder()
                .member(member)
                .nickName("소셜유저")
                .type(MemberType.STUDENT)
                .build();
        memberProfileRepository.save(profile);

        // 새로운 토큰 정보
        TokenDto newToken = TokenDto.builder()
                .accessToken("new-token")
                .refreshToken("new-refresh")
                .tokenType("Bearer")
                .provider("google")
                .providerUserId("google123")
                .expiresIn(3600)
                .email(member.getEmail())
                .build();

        // when
        AuthResultDto result = loginService.socialLogin(newToken);

        // then
        assertThat(result.isNewUser()).isFalse();
        assertThat(result.getProfileId()).isEqualTo(profile.getId());

        // 토큰 업데이트 확인
        SocialAccount updatedAccount = socialAccountRepository.findByProviderAndProviderUserId(
                "google", "google123").orElseThrow();
        assertThat(updatedAccount.getAccessToken()).isEqualTo("new-token");
        assertThat(updatedAccount.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    @DisplayName("새로운 소셜계정으로 로그인하면 계정이 생성된다")
    void socialLoginWithNewAccount() {
        // given
        TokenDto newToken = TokenDto.builder()
                .accessToken("brand-new-token")
                .refreshToken("brand-new-refresh")
                .tokenType("Bearer")
                .provider("kakao")
                .providerUserId("kakao123")
                .expiresIn(3600)
                .email("new_social@example.com")
                .build();

        // when
        AuthResultDto result = loginService.socialLogin(newToken);

        // then
        assertThat(result.isNewUser()).isTrue();
        
        // 새 계정 확인
        SocialAccount newAccount = socialAccountRepository.findByProviderAndProviderUserId(
                "kakao", "kakao123").orElseThrow();
        assertThat(newAccount.getAccessToken()).isEqualTo("brand-new-token");
    }
} 