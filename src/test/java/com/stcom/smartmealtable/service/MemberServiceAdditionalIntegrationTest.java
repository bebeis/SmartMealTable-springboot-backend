package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class MemberServiceAdditionalIntegrationTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    private Member savedMember;

    @BeforeEach
    void setUp() {
        // 테스트에 사용할 회원 데이터 저장
        Member member = Member.builder()
                .fullName("테스트회원")
                .email("additional_test@example.com")
                .rawPassword("Password123!")
                .build();
        savedMember = memberRepository.save(member);
    }

    @Test
    @DisplayName("ID로 회원을 찾을 수 있다")
    void findMemberByMemberId() {
        // when
        Member foundMember = memberService.findMemberByMemberId(savedMember.getId());

        // then
        assertThat(foundMember).isNotNull();
        assertThat(foundMember.getId()).isEqualTo(savedMember.getId());
        assertThat(foundMember.getEmail()).isEqualTo("additional_test@example.com");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 회원 조회 시 예외가 발생한다")
    void findMemberByMemberIdThrowsExceptionWhenIdNotExists() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> memberService.findMemberByMemberId(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 회원입니다");
    }

    @Test
    @DisplayName("비밀번호 확인이 일치하면 정상 처리된다")
    void checkPasswordDoublySuccess() {
        // when & then
        // 예외가 발생하지 않으면 테스트 성공
        memberService.checkPasswordDoubly("Password123!", "Password123!");
    }

    @Test
    @DisplayName("비밀번호 확인이 일치하지 않으면 예외가 발생한다")
    void checkPasswordDoublyThrowsExceptionWhenPasswordsDoNotMatch() {
        // given
        String password = "Password123!";
        String confirmPassword = "DifferentPassword123!";

        // when & then
        assertThatThrownBy(() -> memberService.checkPasswordDoubly(password, confirmPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("검증 비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("중복되지 않은 이메일은 유효성 검증을 통과한다")
    void validateDuplicatedEmailSuccess() {
        // given
        String uniqueEmail = "unique_email@example.com";

        // when & then
        // 예외가 발생하지 않으면 테스트 성공
        memberService.validateDuplicatedEmail(uniqueEmail);
    }

    @Test
    @DisplayName("비밀번호 변경 시 원래 비밀번호가 일치하지 않으면 예외가 발생한다")
    void changePasswordThrowsExceptionWhenOriginalPasswordDoesNotMatch() {
        // given
        String wrongOriginalPassword = "WrongPassword123!";
        String newPassword = "NewPassword123!";

        // when & then
        assertThatThrownBy(() -> memberService.changePassword(savedMember.getId(), wrongOriginalPassword, newPassword))
                .isInstanceOf(PasswordFailedExceededException.class)
                .hasMessage("기존 비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("존재하지 않는 회원 ID로 회원 삭제 시 예외가 발생한다")
    void deleteByMemberIdThrowsExceptionWhenIdNotExists() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> memberService.deleteByMemberId(nonExistentId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원이 존재하지 않습니다");
    }
} 