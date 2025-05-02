package com.stcom.smartmealtable.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MemberPasswordTest {

    @Test
    void 비밀번호_정책_성공() throws Exception {
        String successRawPassword = "abcdefg123";
        assertDoesNotThrow(() ->
                MemberPassword.builder().rawPassword(successRawPassword).build()
        );
    }

    @Test
    void 비밀번호_정책_8자이상() throws Exception {
        String failedRawPassword = "abc123";
        // JUnit5
        checkFailedCase(failedRawPassword);
    }

    private void checkFailedCase(String failedRawPassword) {
        assertThrows(PasswordPolicyException.class, () ->
                MemberPassword.builder().rawPassword(failedRawPassword).build()
        );
    }

    @Test
    void 비밀번호_정책_공백() throws Exception {
        String failedRawPassword = "aa bb124gf";
        checkFailedCase(failedRawPassword);
    }

    @Test
    void 비밀번호_정책_20자이하() throws Exception {
        String failedRawPassword = "aasdafsf124e124241414114214";
        checkFailedCase(failedRawPassword);
    }

    @Test
    void 비밀번호_정책_영어숫자포함() throws Exception {
        String failedRawPassword = "가나다라4asfsadfasd";
        checkFailedCase(failedRawPassword);
    }


    @Test
    void 비밀번호_일치() throws Exception {
        // given
        MemberPassword newPassword = MemberPassword.builder()
                .rawPassword("abcdefg1234")
                .build();
        // then
        assertThat(newPassword.isMatched("abcdefg1234"));
    }

    @Test
    void 비밀번호_변경_실패_이전비밀번호_불일치() throws Exception {
        MemberPassword oldPassword
                = MemberPassword.builder()
                .rawPassword("abcdefg1234")
                .build();

        assertThrows(PasswordFailedExceededException.class,
                () -> oldPassword.changePassword("abcdccc1234", "abcdefg123")); // 실패해야 함.)
    }

    @Test
    void 비밀번호_변경_실패_새비밀번호_정책실패() throws Exception {
        MemberPassword oldPassword
                = MemberPassword.builder()
                .rawPassword("abcdefg1234")
                .build();

        assertThrows(PasswordPolicyException.class,
                () -> oldPassword.changePassword("abcde", "abcdefg1234")); // 실패해야 함.)
    }

    @Test
    void 비밀번호_변경_실패_새비밀번호_기존과동일() throws Exception {
        MemberPassword oldPassword
                = MemberPassword.builder()
                .rawPassword("abcdefg1234")
                .build();

        assertThrows(PasswordPolicyException.class,
                () -> oldPassword.changePassword("abcdefg1234", "abcdefg1234")); // 실패해야 함.)
    }


    @Test
    void 비밀번호_변경_성공() throws Exception {
        MemberPassword password
                = MemberPassword.builder()
                .rawPassword("abcdefg1234")
                .build();

        assertDoesNotThrow(
                () -> password.changePassword("aaabcd1234", "abcdefg1234")); // 실패해야 함.)
        assertTrue(password.isMatched("aaabcd1234"));
    }


}