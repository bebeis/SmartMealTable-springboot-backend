package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.service.MemberService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원 계정 관리(비밀번호 변경, 탈퇴) 전용 컨트롤러.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberAccountController {

    private final MemberService memberService;

    @PatchMapping("/me/password")
    public ApiResponse<Void> changePassword(@UserContext MemberDto memberDto,
                                            @Valid @RequestBody PasswordChangeRequest request)
            throws PasswordPolicyException, PasswordFailedExceededException {
        memberService.checkPasswordDoubly(request.getNewPassword(), request.getConfirmPassword());
        memberService.changePassword(memberDto.getMemberId(), request.getOriginPassword(), request.getNewPassword());
        return ApiResponse.createSuccessWithNoContent();
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> deleteMember(@UserContext MemberDto memberDto) {
        memberService.deleteByMemberId(memberDto.getMemberId());
        return ApiResponse.createSuccessWithNoContent();
    }


    @Data
    @AllArgsConstructor
    public static class PasswordChangeRequest {
        private String originPassword;
        private String newPassword;
        private String confirmPassword;
    }
} 