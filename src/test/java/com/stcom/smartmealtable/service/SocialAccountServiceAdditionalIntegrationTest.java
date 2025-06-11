package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import com.stcom.smartmealtable.repository.MemberRepository;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SocialAccountServiceAdditionalIntegrationTest {

    @Autowired
    private SocialAccountService socialAccountService;

    @Autowired
    private SocialAccountRepository repository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("소셜 계정 토큰을 업데이트 할 수 있다")
    @Rollback
    void updateToken() {
        // given
        Member member = new Member("update@test.com");
        memberRepository.save(member);
        SocialAccount account = SocialAccount.builder()
                .member(member)
                .provider("kakao")
                .providerUserId("u1")
                .tokenType("Bearer")
                .accessToken("old")
                .refreshToken("old_r")
                .tokenExpiresAt(LocalDateTime.now())
                .build();
        repository.save(account);

        // when
        socialAccountService.updateToken(account.getId(), "new", "new_r", LocalDateTime.now().plusDays(1));

        // then
        SocialAccount updated = repository.findById(account.getId()).orElseThrow();
        assertThat(updated.getAccessToken()).isEqualTo("new");
    }

    @Test
    @DisplayName("회원이 연동한 모든 provider 목록을 조회할 수 있다")
    void findAllProviders() {
        // given
        Member member = new Member("providers@test.com");
        memberRepository.save(member);
        repository.save(SocialAccount.builder()
                .member(member).provider("google").providerUserId("g1")
                .tokenType("Bearer").accessToken("a").refreshToken("r")
                .tokenExpiresAt(LocalDateTime.now()).build());
        repository.save(SocialAccount.builder()
                .member(member).provider("kakao").providerUserId("k1")
                .tokenType("Bearer").accessToken("a").refreshToken("r")
                .tokenExpiresAt(LocalDateTime.now()).build());

        // when
        List<String> providers = socialAccountService.findAllProviders(member.getId());

        // then
        assertThat(providers).containsExactlyInAnyOrder("google", "kakao");
    }
} 