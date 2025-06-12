package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.infrastructure.dto.TokenDto;
import com.stcom.smartmealtable.service.dto.AuthResultDto;
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
class LoginServiceIntegrationTest {

    @Autowired
    private LoginService loginService;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("이메일/비밀번호 로그인 성공 후 AuthResultDto 가 반환되어야 한다")
    @Rollback
    void loginWithEmail() throws Exception {
        // given
        Member member = Member.builder()
                .fullName("로그인유저")
                .email("login@test.com")
                .rawPassword("Password1!")
                .build();
        memberService.saveMember(member);

        // when
        AuthResultDto dto = loginService.loginWithEmail("login@test.com", "Password1!");

        // then
        assertThat(dto.getMemberId()).isEqualTo(member.getId());
        assertThat(dto.isNewUser()).isTrue();
    }

    @Test
    @DisplayName("소셜 로그인 시 신규 회원이면 newUser 플래그가 true 여야 한다")
    @Rollback
    void socialLogin() {
        // given
        TokenDto token = TokenDto.builder()
                .accessToken("token")
                .refreshToken("refresh")
                .tokenType("Bearer")
                .provider("google")
                .providerUserId("g123")
                .expiresIn(3600)
                .email("social@test.com")
                .build();

        // when
        AuthResultDto dto = loginService.socialLogin(token);

        // then
        assertThat(dto.isNewUser()).isTrue();
        assertThat(dto.getMemberId()).isNotNull();
    }
} 