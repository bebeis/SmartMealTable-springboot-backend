package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.service.dto.AuthResultDto;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LoginServiceCompleteIntegrationTest {

    @Autowired
    private LoginService loginService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    private Member testMember;
    private Member memberWithProfile;
    private SocialAccount existingSocialAccount;

    @BeforeEach
    void setUp() {
        // 기본 회원 생성
        testMember = Member.builder()
                .email("test@example.com")
                .rawPassword("TestPassword123!")
                .build();
        memberRepository.save(testMember);

        // 프로필이 있는 회원 생성
        memberWithProfile = Member.builder()
                .email("profile@example.com")
                .rawPassword("TestPassword123!")
                .build();
        memberRepository.save(memberWithProfile);

        // 프로필 생성 (linkMember가 memberProfile과 member를 연결함)
        MemberProfile profile = MemberProfile.builder()
                .nickName("TestUser")
                .member(memberWithProfile)
                .type(MemberType.STUDENT)
                .build();

        // 기존 소셜 계정 생성
        existingSocialAccount = SocialAccount.builder()
                .member(testMember)
                .provider("kakao")
                .providerUserId("kakao123")
                .tokenType("Bearer")
                .accessToken("existing_access_token")
                .refreshToken("existing_refresh_token")
                .tokenExpiresAt(LocalDateTime.now().plusHours(1))
                .build();
        socialAccountRepository.save(existingSocialAccount);

        memberRepository.save(memberWithProfile);
        memberProfileRepository.save(profile);
    }

    @Test
    @DisplayName("이메일과 비밀번호로 로그인할 수 있다")
    void loginWithEmail() throws PasswordFailedExceededException {
        // when
        AuthResultDto result = loginService.loginWithEmail("test@example.com", "TestPassword123!");

        // then
        assertThat(result.getMemberId()).isEqualTo(testMember.getId());
        assertThat(result.getProfileId()).isNull();
        assertThat(result.isNewUser()).isTrue();
    }

    @Test
    @DisplayName("프로필이 있는 회원으로 로그인하면 프로필 ID를 반환한다")
    void loginWithEmailWithProfile() throws PasswordFailedExceededException {
        // when
        AuthResultDto result = loginService.loginWithEmail("profile@example.com", "TestPassword123!");

        // then
        assertThat(result.getMemberId()).isEqualTo(memberWithProfile.getId());
        assertThat(result.getProfileId()).isEqualTo(memberWithProfile.getMemberProfile().getId());
        assertThat(result.isNewUser()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 예외가 발생한다")
    void loginWithNonExistentEmail() {
        // when & then
        assertThatThrownBy(() -> loginService.loginWithEmail("nonexistent@example.com", "password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다.");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 예외가 발생한다")
    void loginWithWrongPassword() {
        // when & then
        assertThatThrownBy(() -> loginService.loginWithEmail("test@example.com", "WrongPassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("기존 회원의 소셜 로그인을 할 수 있다")
    void socialLoginExistingMember() {
        // given
        TokenDto tokenDto = TokenDto.builder()
                .accessToken("new_access_token")
                .refreshToken("new_refresh_token")
                .expiresIn(3600)
                .tokenType("Bearer")
                .provider("kakao")
                .providerUserId("kakao123")
                .email("test@example.com")
                .build();

        // when
        AuthResultDto result = loginService.socialLogin(tokenDto);

        // then
        assertThat(result.getMemberId()).isEqualTo(testMember.getId());
        assertThat(result.getProfileId()).isNull();
        assertThat(result.isNewUser()).isTrue();

        // 토큰이 업데이트되었는지 확인
        SocialAccount updatedAccount = socialAccountRepository.findByProviderAndProviderUserId(
                "kakao", "kakao123").orElseThrow();
        assertThat(updatedAccount.getAccessToken()).isEqualTo("new_access_token");
        assertThat(updatedAccount.getRefreshToken()).isEqualTo("new_refresh_token");
    }

    @Test
    @DisplayName("새로운 회원의 소셜 로그인을 할 수 있다")
    void socialLoginNewMember() {
        // given
        TokenDto tokenDto = TokenDto.builder()
                .accessToken("google_access_token")
                .refreshToken("google_refresh_token")
                .expiresIn(3600)
                .tokenType("Bearer")
                .provider("google")
                .providerUserId("google123")
                .email("newuser@example.com")
                .build();

        // when
        AuthResultDto result = loginService.socialLogin(tokenDto);

        // then
        assertThat(result.getMemberId()).isNotNull();
        assertThat(result.getProfileId()).isNull();
        assertThat(result.isNewUser()).isTrue();

        // 새로운 회원이 생성되었는지 확인
        Member newMember = memberRepository.findByEmail("newuser@example.com").orElseThrow();
        assertThat(newMember.getEmail()).isEqualTo("newuser@example.com");

        // 소셜 계정이 생성되었는지 확인
        SocialAccount socialAccount = socialAccountRepository.findByProviderAndProviderUserId(
                "google", "google123").orElseThrow();
        assertThat(socialAccount.getMember().getId()).isEqualTo(newMember.getId());
    }

    @Test
    @DisplayName("기존 소셜 계정이 없는 경우 새로 생성한다")
    void socialLoginCreateNewSocialAccount() {
        // given
        TokenDto tokenDto = TokenDto.builder()
                .accessToken("naver_access_token")
                .refreshToken("naver_refresh_token")
                .expiresIn(3600)
                .tokenType("Bearer")
                .provider("naver")
                .providerUserId("naver456")
                .email("test@example.com")
                .build();

        // when
        AuthResultDto result = loginService.socialLogin(tokenDto);

        // then
        assertThat(result.getMemberId()).isEqualTo(testMember.getId());
        assertThat(result.getProfileId()).isNull();
        assertThat(result.isNewUser()).isTrue();

        // 새로운 소셜 계정이 생성되었는지 확인
        SocialAccount naverAccount = socialAccountRepository.findByProviderAndProviderUserId(
                "naver", "naver456").orElseThrow();
        assertThat(naverAccount.getMember().getId()).isEqualTo(testMember.getId());
        assertThat(naverAccount.getProvider()).isEqualTo("naver");
        assertThat(naverAccount.getProviderUserId()).isEqualTo("naver456");
    }

    @Test
    @DisplayName("프로필이 있는 회원의 소셜 로그인은 프로필 ID를 반환한다")
    void socialLoginMemberWithProfile() {
        // given
        // memberWithProfile에 소셜 계정을 먼저 연결
        SocialAccount googleAccount = SocialAccount.builder()
                .member(memberWithProfile)
                .provider("google")
                .providerUserId("google789")
                .tokenType("Bearer")
                .accessToken("google_access_token")
                .refreshToken("google_refresh_token")
                .tokenExpiresAt(LocalDateTime.now().plusHours(1))
                .build();
        socialAccountRepository.save(googleAccount);

        TokenDto tokenDto = TokenDto.builder()
                .accessToken("updated_google_access_token")
                .refreshToken("updated_google_refresh_token")
                .expiresIn(3600)
                .tokenType("Bearer")
                .provider("google")
                .providerUserId("google789")
                .email("profile@example.com")
                .build();

        // when
        AuthResultDto result = loginService.socialLogin(tokenDto);

        // then
        assertThat(result.getMemberId()).isEqualTo(memberWithProfile.getId());
        assertThat(result.getProfileId()).isEqualTo(memberWithProfile.getMemberProfile().getId());
        assertThat(result.isNewUser()).isFalse();
    }
} 