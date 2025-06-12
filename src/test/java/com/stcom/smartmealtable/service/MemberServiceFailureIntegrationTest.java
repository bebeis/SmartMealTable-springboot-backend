package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.repository.MemberRepository;
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
class MemberServiceFailureIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("존재하지 않는 회원 ID 로 비밀번호 변경 시 예외가 발생해야 한다")
    @Rollback
    void changePassword_memberNotFound() {
        assertThatThrownBy(() -> memberService.changePassword(999L, "old", "new"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회원이 존재하지 않습니다");
    }

    @Test
    @DisplayName("패스워드 중복 확인이 일치하지 않으면 예외가 발생해야 한다")
    void checkPasswordDoublyMismatch() {
        assertThatThrownBy(() -> memberService.checkPasswordDoubly("a", "b"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("회원 삭제시 존재하지 않는 ID 일 경우 예외가 발생한다")
    void deleteMember_notFound() {
        assertThatThrownBy(() -> memberService.deleteByMemberId(999L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("중복 이메일 검증에서 이미 존재하는 이메일이면 예외 발생")
    @Rollback
    void duplicateEmail() throws PasswordPolicyException {
        Member member = Member.builder()
                .fullName("dup")
                .email("dup@test.com")
                .rawPassword("Password1!")
                .build();
        memberRepository.save(member);

        assertThatThrownBy(() -> memberService.validateDuplicatedEmail("dup@test.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }
} 