package com.stcom.smartmealtable.web.member;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.food.FoodCategory;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberGroup;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.exception.PasswordPolicyException;
import com.stcom.smartmealtable.infrastructure.AddressApiService;
import com.stcom.smartmealtable.infrastructure.dto.JwtTokenResponseDto;
import com.stcom.smartmealtable.security.JwtAuthorization;
import com.stcom.smartmealtable.security.JwtTokenService;
import com.stcom.smartmealtable.service.BudgetService;
import com.stcom.smartmealtable.service.FoodPreferenceService;
import com.stcom.smartmealtable.service.MemberService;
import com.stcom.smartmealtable.service.SocialAccountService;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
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

    @GetMapping("/profile")
    public ApiResponse<?> memberProfile(@JwtAuthorization Member member) {
        MemberProfile memberProfile = memberService.findMemberProfileByMemberId(member.getId());
        DailyBudget dailyBudget = budgetService.findRecentDailyBudgetByMemberId(member.getId());
        MonthlyBudget monthlyBudget = budgetService.findRecentMonthlyBudgetByMemberId(member.getId());
        List<String> providers = socialAccountService.findAllProviders(member.getId());
        List<FoodCategory> foodCategories = foodPreferenceService.findPreferredFoodCategories(member);
        MemberProfileResponse memberProfileResponse =
                buildMemberProfileResponse(member, memberProfile, foodCategories, dailyBudget, monthlyBudget);
        return ApiResponse.createSuccess(memberProfileResponse);
    }

    private MemberProfileResponse buildMemberProfileResponse(Member member, MemberProfile memberProfile,
                                                             List<FoodCategory> foodCategories, DailyBudget dailyBudget,
                                                             MonthlyBudget monthlyBudget) {
        return MemberProfileResponse.builder()
                .memberGroup(memberProfile.getMemberGroup())
                .groupName(memberProfile.getGroupName())
                .email(member.getEmail())
                .nickName(memberProfile.getNickName())
                .preferredFoodCategory(foodCategories)
                .dailyLimitAmount(dailyBudget.getLimit().longValue())
                .dailyAvailableAmount(dailyBudget.getAvailableAmount().longValue())
                .monthlyLimitAmount(monthlyBudget.getLimit().longValue())
                .monthlyAvailableAmount(monthlyBudget.getAvailableAmount().longValue())
                .addressList(memberProfile.getAddressHistory().stream()
                        .map(a -> a.getRoadAddress() + a.getDetailAddress())
                        .toList())
                .build();
    }

    private Address getAddressFromApiService(AddressRequest request) {
        com.stcom.smartmealtable.infrastructure.dto.AddressRequest dto = new com.stcom.smartmealtable.infrastructure.dto.AddressRequest(
                request.getRoadAddress(), AddressType.HOME, "집",
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
        private String groupName;
        private AddressRequest homeAddress;
        private List<FoodCategory> foodPreference;
        private List<FoodCategory> hateFoods;
        private Long dailyLimitAmount;
        private Long monthlyLimitAmount;

        public MemberProfile toEntity() {
            return MemberProfile.builder()
                    .memberGroup(groupType)
                    .groupName(groupName)
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

    @Data
    @NoArgsConstructor
    static class MemberProfileResponse {
        private String nickName;
        private List<FoodCategory> preferredFoodCategory;
        private String email;
        private List<String> addressList;
        private MemberGroup memberGroup;
        private String groupName;
        private Long dailyAvailableAmount;
        private Long dailyLimitAmount;
        private Long monthlyAvailableAmount;
        private Long monthlyLimitAmount;

        @Builder
        public MemberProfileResponse(String nickName, List<FoodCategory> preferredFoodCategory, String email,
                                     List<String> addressList, MemberGroup memberGroup, String groupName,
                                     Long dailyAvailableAmount,
                                     Long dailyLimitAmount, Long monthlyAvailableAmount, Long monthlyLimitAmount) {
            this.nickName = nickName;
            this.preferredFoodCategory = preferredFoodCategory;
            this.email = email;
            this.addressList = addressList;
            this.memberGroup = memberGroup;
            this.groupName = groupName;
            this.dailyAvailableAmount = dailyAvailableAmount;
            this.dailyLimitAmount = dailyLimitAmount;
            this.monthlyAvailableAmount = monthlyAvailableAmount;
            this.monthlyLimitAmount = monthlyLimitAmount;
        }
    }


}
