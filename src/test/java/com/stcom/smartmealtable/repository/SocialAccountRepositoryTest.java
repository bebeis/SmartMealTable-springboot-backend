package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.social.SocialAccount;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class SocialAccountRepositoryTest {

    @Autowired
    private SocialAccountRepository repository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("소셜 계정을 저장하고 provider/userId 로 조회할 수 있어야 한다")
    void saveAndFind() {
        // given
        Member member = new Member("social@example.com");
        memberRepository.save(member);

        SocialAccount account = SocialAccount.builder()
                .member(member)
                .provider("google")
                .providerUserId("12345")
                .tokenType("Bearer")
                .accessToken("access")
                .refreshToken("refresh")
                .tokenExpiresAt(LocalDateTime.now().plusDays(1))
                .build();
        repository.save(account);
        em.flush();
        em.clear();

        // when
        Optional<SocialAccount> found = repository.findByProviderAndProviderUserId("google", "12345");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getMember().getEmail()).isEqualTo("social@example.com");
    }
} 