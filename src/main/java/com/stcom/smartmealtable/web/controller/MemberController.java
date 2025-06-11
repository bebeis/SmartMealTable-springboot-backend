package com.stcom.smartmealtable.web.controller;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 과거 테스트 코드 및 바이너리 호환성을 위한 DTO 컨테이너임.
 */
@Deprecated
public final class MemberController {

    private MemberController() {
    }

    @Data
    @AllArgsConstructor
    public static class CreateMemberRequest {
        @Email
        private String email;
        private String password;
        private String confirmPassword;
        private String fullName;
    }

    @Data
    @AllArgsConstructor
    public static class EditMemberRequest {
        private String originPassword;
        private String newPassword;
        private String confirmPassword;
    }

    @Data
    @AllArgsConstructor
    public static class TermAgreementDto {
        private Long termId;
        private Boolean isAgreed;
    }
} 