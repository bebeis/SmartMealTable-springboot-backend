package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.exception.PasswordFailedExceededException;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.infrastructure.AddressApiService;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.BudgetService;
import com.stcom.smartmealtable.service.FoodPreferenceService;
import com.stcom.smartmealtable.service.MemberService;
import com.stcom.smartmealtable.service.SocialAccountService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenService jwtTokenService;
    private final AddressApiService addressApiService;
    private final BudgetService budgetService;
    private final SocialAccountService socialAccountService;
    private final FoodPreferenceService foodPreferenceService;

    @GetMapping("/email/check")
    public ResponseEntity<ApiResponse<?>> checkEmail(@Email @RequestParam String email) {
        memberService.validateDuplicatedEmail(email);
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping()
    public ApiResponse<?> createMember(@Valid @RequestBody CreateMemberRequest request,
                                       BindingResult bindingResult) throws PasswordPolicyException {
        memberService.validateDuplicatedEmail(request.getEmail());
        memberService.checkPasswordDoubly(request.getPassword(), request.getConfirmPassword());

        Member member = Member.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .rawPassword(request.getPassword())
                .build();

        memberService.saveMember(member);
        JwtTokenResponseDto tokenDto = jwtTokenService.createTokenDto(member.getId(), null);
        tokenDto.setNewUser(true);
        return ApiResponse.createSuccess(tokenDto);
    }

    @PatchMapping("/me")
    public ApiResponse<?> editMember(@UserContext MemberDto memberDto, @Valid @RequestBody EditMemberRequest request,
                                     BindingResult bindingResult)
            throws PasswordPolicyException, PasswordFailedExceededException {
        memberService.checkPasswordDoubly(request.getNewPassword(), request.getConfirmPassword());
        memberService.changePassword(memberDto.getMemberId(), request.getOriginPassword(), request.getNewPassword());
        return ApiResponse.createSuccessWithNoContent();
    }

    @DeleteMapping("/me")
    public ApiResponse<?> deleteMember(@UserContext MemberDto memberDto) {
        memberService.deleteByMemberId(memberDto.getMemberId());
        return ApiResponse.createSuccessWithNoContent();
    }


    @Data
    @AllArgsConstructor
    static class CreateMemberRequest {

        @Email
        private String email;
        private String password;
        private String confirmPassword;
        private String fullName;
    }

    @Data
    @AllArgsConstructor
    static class EditMemberRequest {

        private String originPassword;
        private String newPassword;
        private String confirmPassword;
    }

}
