package com.stcom.smartmealtable.domain.member;

import com.stcom.smartmealtable.domain.common.BaseTimeEntity;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Email
    private String email;

    @Embedded
    private MemberPassword password;

    private String fullName;


    // TODO: 이메일 인증 기능 구현해야함
    private boolean isEmailVerified = true;

    @OneToOne(mappedBy = "member")
    private MemberProfile memberProfile;

    public Member(String email) {
        this.email = email;
    }

    @Builder
    public Member(String fullName, String email, String rawPassword) throws PasswordPolicyException {
        this.fullName = fullName;
        this.email = email;
        this.password = new MemberPassword(rawPassword);
    }

    protected void linkMemberProfile(MemberProfile profile) {
        this.memberProfile = profile;
    }

    public void changePassword(String rawOldPassword, String rawNewPassword)
            throws PasswordFailedExceededException, PasswordPolicyException {
        if (rawOldPassword.isBlank() || rawNewPassword.isBlank()) {
            throw new IllegalArgumentException("빈 비밀번호를 입력했습니다");
        }
        password.changePassword(rawNewPassword, rawOldPassword);
    }

    public boolean isMatchedPassword(final String rawPassword) throws PasswordFailedExceededException {
        return password.isMatched(rawPassword);
    }

    public boolean isEmailVerified() {
        return isEmailVerified;
    }

    public void verifyEmail() {
        this.isEmailVerified = true;
    }

    public boolean isProfileRegistered() {
        return memberProfile == null;
    }


}
