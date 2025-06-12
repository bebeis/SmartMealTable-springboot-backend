package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.repository.SocialAccountRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class SocialAccountServiceIntegrationTest {

    @Autowired
    private SocialAccountService socialAccountService;

    @Autowired
    private SocialAccountRepository repository;

    @Test
    @DisplayName("새로운 사용자를 소셜 계정으로 생성하고 조회할 수 있다")
    void createNewMemberAndLink() {
        // given
        TokenDto token = TokenDto.builder()
                .accessToken("access")
                .refreshToken("refresh")
                .expiresIn(3600)
                .tokenType("Bearer")
                .provider("kakao")
                .providerUserId("k123")
                .email("socialnew@example.com")
                .build();

        // when
        socialAccountService.createNewMemberAndLinkSocialAccount(token);

        // then
        var saved = repository.findByProviderAndProviderUserId("kakao", "k123");
        assertThat(saved).isPresent();
        assertThat(saved.get().getMember().getEmail()).isEqualTo("socialnew@example.com");
        assertThat(saved.get().getTokenExpiresAt()).isAfter(LocalDateTime.now());
    }
} 