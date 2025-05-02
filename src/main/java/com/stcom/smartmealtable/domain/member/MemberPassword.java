package com.stcom.smartmealtable.domain.member;


import jakarta.persistence.Embeddable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
public class MemberPassword {

    private final static int MAX_FAILED_COUNT = 5;
    private final static long TTL = 1209_604; // 2 weeks

    private String password_hash;

    private int failedCount;

    private LocalDateTime expirationDate;

    private long ttl;

    @Builder
    public MemberPassword(String rawPassword) throws PasswordPolicyException {
        checkPasswordPolicy(rawPassword);
        this.password_hash = encodePassword(rawPassword);
        this.expirationDate = LocalDateTime.now().plusSeconds(TTL);
        this.ttl = TTL; // 2 weeks
        this.failedCount = 0;
    }

    public void checkPasswordPolicy(String rawPassword) throws PasswordPolicyException {
        if (rawPassword.contains(" ")) {
            throw new PasswordPolicyException("비밀번호는 공백을 포함할 수 없습니다");
        }

        if (rawPassword.length() < 8) {
            throw new PasswordPolicyException("비밀번호는 8자 이상이어야 합니다.");
        }

        if (rawPassword.length() > 20) {
            throw new PasswordPolicyException("비밀번호는 최대 20자까지 가능합니다.");
        }

        if (!rawPassword.matches("^[A-Za-z0-9]+$")) {
            throw new PasswordPolicyException("비밀번호는 영문자(A–Z, a–z)와 숫자(0–9)로만 구성되어야 합니다.");
        }
    }

    public boolean isMatched(final String rawPassword) throws PasswordFailedExceededException {
        checkFailedCount();
        final boolean matches = isMatches(rawPassword);
        updateFailedCount(matches);
        return matches;
    }

    public boolean isPasswordExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }

    private void checkFailedCount() throws PasswordFailedExceededException {
        if (failedCount >= MAX_FAILED_COUNT) {
            throw new PasswordFailedExceededException();
        }
    }

    private boolean isMatches(String rawPassword) {
        return password_hash.equals(encodePassword(rawPassword));
    }

    private void updateFailedCount(boolean matches) {
        if (matches) {
            failedCount = 0;
            return;
        }

        failedCount++;
    }

    public void changePassword(final String newPassword, final String oldPassword)
            throws PasswordFailedExceededException, PasswordPolicyException {
        if (!isMatches(oldPassword)) {
            throw new PasswordFailedExceededException("기존 비밀번호가 일치하지 않습니다");
        }
        if (newPassword.equals(oldPassword)) {
            throw new PasswordPolicyException("기존 비밀번호와 새 비밀번호가 동일합니다. 기존 비밀번호와 동일하지 않은 비밀번호로 설정해주세요");
        }
        checkPasswordPolicy(newPassword);
        password_hash = encodePassword(newPassword);
        extendExpirationDate();
    }

    private void extendExpirationDate() {
        expirationDate = LocalDateTime.now().plusSeconds(ttl);
    }

    private String encodePassword(String newPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(newPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16))
                    .append(Character.forDigit((b & 0xF), 16));
        }
        return sb.toString();
    }
}
