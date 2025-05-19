package com.stcom.smartmealtable.web.member;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.domain.food.FoodCategory;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberGroup;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.infrastructure.AddressApiService;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequestDto;
import com.stcom.smartmealtable.security.JwtAuthorization;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.MemberService;
import com.stcom.smartmealtable.service.dto.token.JwtTokenResponseDto;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
        JwtTokenResponseDto tokenDto = jwtTokenService.createTokenDto(member.getId());
        tokenDto.setNewUser(true);
        return ApiResponse.createSuccess(tokenDto);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/profile")
    public ApiResponse<?> createMemberProfile(@JwtAuthorization Member member,
                                              @RequestBody CreateMemberProfileRequest request) {
        // request 주소로 부터 위도, 경도 얻기(카카오 local api)
        Address address = getAddressFromApiService(request.getHomeAddress());
        MemberProfile memberProfile = request.toEntity();
        memberProfile.addAddress(address);
        memberService.linkMember(member.getId(), memberProfile);

        return ApiResponse.createSuccessWithNoContent();
    }

    private Address getAddressFromApiService(AddressRequest request) {
        AddressRequestDto dto = new AddressRequestDto(request.getRoadAddress(), AddressType.HOME, "집",
                request.getDetailAddress());
        return addressApiService.createAddressFromRequest(dto);
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
    @NoArgsConstructor
    static class CreateMemberProfileRequest {
        private MemberGroup groupType;
        private Long groupCode;
        private AddressRequest homeAddress;
        private List<FoodCategory> foodPreference;
        private List<FoodCategory> hateFoods;
        private Long dailyLimitAmount;
        private Long monthlyLimitAmount;

        public MemberProfile toEntity() {
            return MemberProfile.builder()
                    .memberGroup(groupType)
                    .groupCode(groupCode)
                    .nickName("test")
                    .addressHistory(new ArrayList<>())
                    .build();
        }
    }

    @Data
    @AllArgsConstructor
    static class AddressRequest {
        private int zonecode;
        private String lotNumberAddress;
        private String roadAddress;
        private String detailAddress;
    }


}
