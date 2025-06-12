package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SocialAccountServiceCompleteIntegrationTest {

    @Autowired
    private SocialAccountService socialAccountService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SocialAccountRepository socialAccountRepository;

    private Member testMember;
    private SocialAccount kakaoAccount;
    private SocialAccount naverAccount;

    @BeforeEach
    void setUp() {
        testMember = new Member("test@example.com");
        memberRepository.save(testMember);

        kakaoAccount = SocialAccount.builder()
                .member(testMember)
                .provider("kakao")
                .providerUserId("kakao123")
                .tokenType("Bearer")
                .accessToken("kakao_access_token")
                .refreshToken("kakao_refresh_token")
                .tokenExpiresAt(LocalDateTime.now().plusHours(1))
                .build();

        naverAccount = SocialAccount.builder()
                .member(testMember)
                .provider("naver")
                .providerUserId("naver456")
                .tokenType("Bearer")
                .accessToken("naver_access_token")
                .refreshToken("naver_refresh_token")
                .tokenExpiresAt(LocalDateTime.now().plusHours(1))
                .build();

        socialAccountRepository.save(kakaoAccount);
        socialAccountRepository.save(naverAccount);
    }

    @Test
    @DisplayName("새로운 회원과 소셜 계정을 생성할 수 있다")
    void createNewMemberAndLinkSocialAccount() {
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
        socialAccountService.createNewMemberAndLinkSocialAccount(tokenDto);

        // then
        Member newMember = memberRepository.findByEmail("newuser@example.com").orElseThrow();
        assertThat(newMember.getEmail()).isEqualTo("newuser@example.com");

        SocialAccount socialAccount = socialAccountRepository.findByProviderAndProviderUserId(
                "google", "google123").orElseThrow();
        assertThat(socialAccount.getProvider()).isEqualTo("google");
        assertThat(socialAccount.getProviderUserId()).isEqualTo("google123");
        assertThat(socialAccount.getMember().getId()).isEqualTo(newMember.getId());
    }

    @Test
    @DisplayName("기존 회원에게 소셜 계정을 연결할 수 있다")
    void linkSocialAccount() {
        // given
        TokenDto tokenDto = TokenDto.builder()
                .accessToken("google_access_token_2")
                .refreshToken("google_refresh_token_2")
                .expiresIn(3600)
                .tokenType("Bearer")
                .provider("google")
                .providerUserId("google789")
                .email(testMember.getEmail())
                .build();

        // when
        socialAccountService.linkSocialAccount(tokenDto);

        // then
        SocialAccount linkedAccount = socialAccountRepository.findByProviderAndProviderUserId(
                "google", "google789").orElseThrow();
        assertThat(linkedAccount.getProvider()).isEqualTo("google");
        assertThat(linkedAccount.getProviderUserId()).isEqualTo("google789");
        assertThat(linkedAccount.getMember().getId()).isEqualTo(testMember.getId());
    }

    @Test
    @DisplayName("존재하지 않는 회원으로 소셜 계정을 연결하려고 하면 예외가 발생한다")
    void linkSocialAccountWithNonExistentMember() {
        // given
        TokenDto tokenDto = TokenDto.builder()
                .accessToken("google_access_token_3")
                .refreshToken("google_refresh_token_3")
                .expiresIn(3600)
                .tokenType("Bearer")
                .provider("google")
                .providerUserId("google999")
                .email("nonexistent@example.com")
                .build();

        // when & then
        assertThatThrownBy(() -> socialAccountService.linkSocialAccount(tokenDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("회원 엔티티가 존재하지 않은 상태로 소셜 계정 연결을 시도했습니다.");
    }

    @Test
    @DisplayName("소셜 계정을 조회할 수 있다")
    void findSocialAccount() {
        // when
        SocialAccount foundAccount = socialAccountService.findSocialAccount("kakao", "kakao123");

        // then
        assertThat(foundAccount).isNotNull();
        assertThat(foundAccount.getProvider()).isEqualTo("kakao");
        assertThat(foundAccount.getProviderUserId()).isEqualTo("kakao123");
        assertThat(foundAccount.getMember().getId()).isEqualTo(testMember.getId());
    }

    @Test
    @DisplayName("존재하지 않는 소셜 계정을 조회하면 null을 반환한다")
    void findSocialAccountNotFound() {
        // when
        SocialAccount foundAccount = socialAccountService.findSocialAccount("twitter", "twitter123");

        // then
        assertThat(foundAccount).isNull();
    }

    @Test
    @DisplayName("신규 사용자인지 확인할 수 있다")
    void isNewUser() {
        // when
        boolean isNew = socialAccountService.isNewUser("twitter", "twitter123");

        // then
        assertThat(isNew).isTrue();
    }

    @Test
    @DisplayName("기존 사용자인지 확인할 수 있다")
    void isExistingUser() {
        // when
        boolean isNew = socialAccountService.isNewUser("kakao", "kakao123");

        // then
        assertThat(isNew).isFalse();
    }

    @Test
    @DisplayName("토큰을 업데이트할 수 있다")
    void updateToken() {
        // given
        String newAccessToken = "new_kakao_access_token";
        String newRefreshToken = "new_kakao_refresh_token";
        LocalDateTime newExpiresAt = LocalDateTime.now().plusHours(2);

        // when
        socialAccountService.updateToken(kakaoAccount.getId(), newAccessToken, newRefreshToken, newExpiresAt);

        // then
        SocialAccount updatedAccount = socialAccountRepository.findById(kakaoAccount.getId()).orElseThrow();
        assertThat(updatedAccount.getAccessToken()).isEqualTo(newAccessToken);
        assertThat(updatedAccount.getRefreshToken()).isEqualTo(newRefreshToken);
        assertThat(updatedAccount.getTokenExpiresAt()).isEqualTo(newExpiresAt);
    }

    @Test
    @DisplayName("존재하지 않는 소셜 계정의 토큰을 업데이트하려고 하면 예외가 발생한다")
    void updateTokenWithInvalidAccountId() {
        // given
        Long invalidAccountId = 99999L;
        String accessToken = "test_token";
        String refreshToken = "test_refresh";
        LocalDateTime expiresAt = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> socialAccountService.updateToken(invalidAccountId, accessToken, refreshToken, expiresAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("확인되지 않은 계정입니다");
    }

    @Test
    @DisplayName("회원의 모든 소셜 제공자를 조회할 수 있다")
    void findAllProviders() {
        // when
        List<String> providers = socialAccountService.findAllProviders(testMember.getId());

        // then
        assertThat(providers).hasSize(2);
        assertThat(providers).containsExactlyInAnyOrder("kakao", "naver");
    }

    @Test
    @DisplayName("소셜 계정이 없는 회원의 제공자 목록은 빈 리스트를 반환한다")
    void findAllProvidersForMemberWithNoSocialAccounts() {
        // given
        Member memberWithoutSocial = new Member("nosocial@example.com");
        memberRepository.save(memberWithoutSocial);

        // when
        List<String> providers = socialAccountService.findAllProviders(memberWithoutSocial.getId());

        // then
        assertThat(providers).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 회원의 제공자를 조회하면 빈 리스트를 반환한다")
    void findAllProvidersForNonExistentMember() {
        // given
        Long nonExistentMemberId = 99999L;

        // when
        List<String> providers = socialAccountService.findAllProviders(nonExistentMemberId);

        // then
        assertThat(providers).isEmpty();
    }
} 