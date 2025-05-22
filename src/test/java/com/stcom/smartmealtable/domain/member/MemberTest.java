package com.stcom.smartmealtable.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    @DisplayName("회원 생성 시 이메일, 이름, 비밀번호가 정상적으로 설정되어야 한다")
    void createMember() throws PasswordPolicyException {
        // given
        String email = "test@example.com";
        String fullName = "홍길동";
        String password = "Password123!";

        // when
        Member member = Member.builder()
                .email(email)
                .fullName(fullName)
                .rawPassword(password)
                .build();

        // then
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getFullName()).isEqualTo(fullName);
        assertThat(member.isEmailVerified()).isTrue(); // 기본값 true
    }

    @Test
    @DisplayName("유효하지 않은 형식의 비밀번호로 회원을 생성하면 예외가 발생한다")
    void createMemberWithInvalidPassword() {
        // given
        String email = "test@example.com";
        String fullName = "홍길동";
        String weakPassword = "123456"; // 취약한 비밀번호

        // when & then
        assertThatThrownBy(() -> Member.builder()
                .email(email)
                .fullName(fullName)
                .rawPassword(weakPassword)
                .build())
                .isInstanceOf(PasswordPolicyException.class);
    }

    @Test
    @DisplayName("비밀번호 변경이 정상적으로 동작해야 한다")
    void changePassword() throws PasswordPolicyException, PasswordFailedExceededException {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .fullName("홍길동")
                .rawPassword("Password123!")
                .build();

        // when
        member.changePassword("Password123!", "NewPassword123!");

        // then
        assertThat(member.isMatchedPassword("NewPassword123!")).isTrue();
    }

    @Test
    @DisplayName("잘못된 비밀번호로 비밀번호 변경 시 예외가 발생해야 한다")
    void changePasswordWithIncorrectPassword() throws PasswordPolicyException {
        // given
        Member member = Member.builder()
                .email("test@example.com")
                .fullName("홍길동")
                .rawPassword("Password123!")
                .build();

        // when & then
        assertThatThrownBy(() -> member.changePassword("WrongPassword123!", "NewPassword123!"))
                .isInstanceOf(PasswordFailedExceededException.class);
    }

    @Test
    @DisplayName("이메일 인증 상태가 정상적으로 변경되어야 한다")
    void verifyEmail() throws PasswordPolicyException {
        // given
        Member member = new Member("test@example.com");

        // when
        member.verifyEmail();

        // then
        assertThat(member.isEmailVerified()).isTrue();
    }

    @Test
    @DisplayName("회원 비밀번호 연속 실패 5회까지는 false 반환하고, 6회 시 예외 발생해야 한다")
    void 비밀번호_연속_실패_제한_초과() throws PasswordPolicyException, PasswordFailedExceededException {
        Member member = Member.builder()
                .email("test@example.com")
                .fullName("홍길동")
                .rawPassword("Password123!")
                .build();
        for (int i = 0; i < 5; i++) {
            assertThat(member.isMatchedPassword("WrongPassword")).isFalse();
        }
        assertThatThrownBy(() -> member.isMatchedPassword("WrongPassword"))
                .isInstanceOf(PasswordFailedExceededException.class);
    }
} 