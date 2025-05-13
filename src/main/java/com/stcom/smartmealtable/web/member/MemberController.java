package com.stcom.smartmealtable.web.member;

import com.stcom.smartmealtable.domain.common.Address;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberGroup;
import com.stcom.smartmealtable.domain.member.MemberService;
import com.stcom.smartmealtable.domain.member.PasswordPolicyException;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import com.stcom.smartmealtable.web.dto.token.JwtTokenResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenService jwtTokenService;

    @GetMapping("/email/check")
    public ResponseEntity<ApiResponse<?>> checkEmail(@Email @RequestParam String email) {
        if (memberService.isEmailExists(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.createError("이미 존재하는 이메일입니다."));
        }
        return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoContent());
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<?>> createMember(@Valid @RequestBody CreateMemberRequest request,
                                                       BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(ApiResponse.createFail(bindingResult));
        }

        if (memberService.isEmailExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.createError("이미 존재하는 이메일입니다."));
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.createError("비밀번호가 일치하지 않습니다."));
        }

        Member member;
        try {
            member = Member.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .rawPassword(request.getPassword())
                    .build();
        } catch (PasswordPolicyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.createError(e.getMessage()));
        }
        memberService.saveMember(member);
        JwtTokenResponseDto tokenDto = jwtTokenService.createTokenDto(member.getId());
        tokenDto.setNewUser(true);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.createSuccess(tokenDto));
    }

//    @PostMapping("/profile")
//    public ApiResponse<?> createMemberProfile(@JwtAuthorization Member member,
//                                              @RequestBody CreateMemberProfileRequest request) {
//
//    }

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
    static class CreateMemberProfileRequest {
        private MemberGroup groupType;
        private Long groupCode;
        private Address homeAddress;
        private Map<String, String> foodPreference;
        private List<String> hateFoods;
        private Long dailyLimitAmount;
        private Long monthlyLimitAmount;
    }


}
