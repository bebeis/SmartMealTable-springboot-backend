package com.stcom.smartmealtable.domain.member;

import com.stcom.smartmealtable.common.BaseEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;

@Entity
@Table(name = "MEMBER_AUTH")
public class Member extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Email
    private String email;

    @Embedded
    private MemberPassword password;

    private boolean isEmailVerified;

    public void changePassword(String rawOldPassword, String rawNewPassword)
            throws PasswordFailedExceededException, PasswordPolicyException {
        if (rawOldPassword.isBlank() || rawNewPassword.isBlank()) {
            throw new IllegalArgumentException("빈 비밀번호를 입력했습니다");
        }
        password.changePassword(rawOldPassword, rawNewPassword);
    }

    public boolean isMatchedPassword(final String rawPassword) throws PasswordFailedExceededException {
        return password.isMatched(rawPassword);
    }
}
