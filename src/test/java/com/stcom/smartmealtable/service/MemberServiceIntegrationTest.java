package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

/**
 * MemberService 통합 테스트 – Mock 사용 없이 실제 DB 상호작용 검증.
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberServiceIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원 생성 후 중복 이메일 검증 시 예외가 발생해야 한다")
    @Rollback
    void validateDuplicatedEmail() throws Exception {
        // given
        Member member = Member.builder()
                .fullName("홍길동")
                .email("duplicate@example.com")
                .rawPassword("Password1!")
                .build();
        memberService.saveMember(member);

        // when & then
        assertThatThrownBy(() -> memberService.validateDuplicatedEmail("duplicate@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 이메일 입니다");
    }

    @Test
    @DisplayName("비밀번호 변경이 정상적으로 수행되어야 한다")
    @Rollback
    void changePassword() throws Exception {
        // given
        Member member = Member.builder()
                .fullName("김철수")
                .email("changepw@example.com")
                .rawPassword("Origin123!")
                .build();
        memberRepository.save(member);

        // when
        memberService.changePassword(member.getId(), "Origin123!", "NewPassword1!");

        // then
        Member changed = memberRepository.findById(member.getId()).orElseThrow();
        assertThat(changed.isMatchedPassword("NewPassword1!")).isTrue();
    }

    @Test
    @DisplayName("회원 삭제가 모든 연관 데이터와 함께 정상적으로 동작해야 한다")
    @Rollback
    void deleteMember() throws PasswordPolicyException, PasswordFailedExceededException {
        // given
        Member member = Member.builder()
                .fullName("이영희")
                .email("deleteme@example.com")
                .rawPassword("Password1!")
                .build();
        memberRepository.save(member);

        // when
        memberService.deleteByMemberId(member.getId());

        // then
        assertThat(memberRepository.findById(member.getId())).isEmpty();
    }
} 