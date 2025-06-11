package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.Address.Address;
import com.stcom.smartmealtable.domain.Address.AddressType;
import com.stcom.smartmealtable.domain.Budget.Budget;
import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.food.MemberCategoryPreference;
import com.stcom.smartmealtable.domain.food.PreferenceType;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.infrastructure.AddressApiService;
import com.stcom.smartmealtable.infrastructure.dto.AddressRequest;
import com.stcom.smartmealtable.service.BudgetService;
import com.stcom.smartmealtable.service.MemberCategoryPreferenceService;
import com.stcom.smartmealtable.service.MemberProfileService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/profiles")
public class MemberProfileController {

    private final MemberProfileService memberProfileService;
    private final AddressApiService addressApiService;
    private final MemberCategoryPreferenceService memberCategoryPreferenceService;
    private final BudgetService budgetService;

    @GetMapping("/me")
    public ApiResponse<MemberProfilePageResponse> getMemberProfilePageInfo(@UserContext MemberDto memberDto) {
        MemberProfile profile = memberProfileService.getProfileFetch(memberDto.getProfileId());
        return ApiResponse.createSuccess(new MemberProfilePageResponse(profile, memberDto));
    }

    @PostMapping()
    public ApiResponse<Void> createMemberProfile(@UserContext MemberDto memberDto,
                                                 @Validated @RequestBody MemberProfileRequest request) {
        memberProfileService.createProfile(request.getNickName(), memberDto.getMemberId(), request.getMemberType(),
                request.getGroupId());
        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/me")
    public ApiResponse<Void> changeMemberProfile(@UserContext MemberDto memberDto,
                                                 @Validated @RequestBody MemberProfileRequest request) {
        memberProfileService.changeProfile(memberDto.getProfileId(), request.getNickName(), request.getMemberType(),
                request.getGroupId());
        return ApiResponse.createSuccessWithNoContent();
    }

    @PostMapping("/me/addresses/{id}/primary")
    public ApiResponse<Void> changePrimaryAddress(@UserContext MemberDto memberDto,
                                                  @PathVariable("id") Long addressId) {
        memberProfileService.changeAddressToPrimary(memberDto.getProfileId(), addressId);
        return ApiResponse.createSuccessWithNoContent();
    }

    @PostMapping("/me/addresses")
    public ApiResponse<Void> registerAddress(@UserContext MemberDto memberDto, MemberAddressCURequest request) {
        Address address = addressApiService.createAddressFromRequest(request.toAddressApiRequest());
        memberProfileService.saveNewAddress(memberDto.getProfileId(), address, request.getAlias(),
                request.getAddressType());
        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/me/addresses/{id}")
    public ApiResponse<Void> changeAddress(@UserContext MemberDto memberDto, @PathVariable("id") Long addressId,
                                           MemberAddressCURequest request) {
        Address address = addressApiService.createAddressFromRequest(request.toAddressApiRequest());
        memberProfileService.changeAddress(memberDto.getProfileId(), addressId, address, request.getAlias(),
                request.getAddressType());
        return ApiResponse.createSuccessWithNoContent();
    }

    @DeleteMapping("/me/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@UserContext MemberDto memberDto, @PathVariable("id") Long addressId) {
        memberProfileService.deleteAddress(memberDto.getProfileId(), addressId);
        return ApiResponse.createSuccessWithNoContent();
    }

    @GetMapping("/me/preferences")
    public ApiResponse<PreferencesResponse> getCategoryPreferences(@UserContext MemberDto memberDto) {
        List<MemberCategoryPreference> preferences =
                memberCategoryPreferenceService.getPreferences(memberDto.getProfileId());

        List<CategoryPreferenceDto> liked = preferences.stream()
                .filter(p -> p.getType() == PreferenceType.LIKE)
                .map(p -> new CategoryPreferenceDto(
                        p.getCategory().getId(),
                        p.getCategory().getName(),
                        p.getPriority()))
                .toList();

        List<CategoryPreferenceDto> disliked = preferences.stream()
                .filter(p -> p.getType() == PreferenceType.DISLIKE)
                .map(p -> new CategoryPreferenceDto(
                        p.getCategory().getId(),
                        p.getCategory().getName(),
                        p.getPriority()))
                .toList();

        return ApiResponse.createSuccess(new PreferencesResponse(liked, disliked));
    }

    @PostMapping("/me/preferences")
    public ApiResponse<Void> saveCategoryPreferences(@UserContext MemberDto memberDto,
                                                     @RequestBody PreferencesRequest request) {
        memberCategoryPreferenceService.savePreferences(
                memberDto.getProfileId(),
                request.getLiked(),
                request.getDisliked());
        return ApiResponse.createSuccessWithNoContent();
    }

    /**
     * 일별 예산 조회
     *
     * @param memberDto
     * @param date      (ISO_LOCAL_DATE 형식, 예: 2025-06-01)
     * @return
     */
    @GetMapping("/me/budgets/daily/{date}")
    public ApiResponse<DailyBudgetResponse> dailyBudgetByDate(@UserContext MemberDto memberDto,
                                                              @PathVariable("date") String date) {
        DailyBudget dailyBudget = budgetService.getDailyBudgetBy(memberDto.getProfileId(), LocalDate.parse(date));
        return ApiResponse.createSuccess(DailyBudgetResponse.of(dailyBudget));
    }

    @PutMapping("/me/budgets/daily/{date}/default")
    public ApiResponse<Void> registerDefaultDailyBudget(@UserContext MemberDto memberDto,
                                                        @PathVariable("date") String date,
                                                        @RequestParam("limit") Long limit) {
        budgetService.registerDefaultDailyBudgetBy(memberDto.getProfileId(), limit, LocalDate.parse(date));
        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/me/budgets/daily/{date}")
    public ApiResponse<Void> editDailyBudget(@UserContext MemberDto memberDto,
                                             @PathVariable("date") String date,
                                             @RequestParam("limit") Long limit) {
        budgetService.editDailyBudgetCustom(memberDto.getProfileId(), LocalDate.parse(date), limit);
        return ApiResponse.createSuccessWithNoContent();
    }

    // 해당 일자가 속한 일일 예산 주간 데이터 조회
    @GetMapping("/me/budgets/daily/{date}/week")
    public ApiResponse<List<DailyBudgetResponse>> dailyBudgetWeekByDate(@UserContext MemberDto memberDto,
                                                                        @PathVariable("date") String date) {
        List<DailyBudget> dailyBudgets = budgetService.getDailyBudgetsByWeek(memberDto.getProfileId(),
                LocalDate.parse(date));

        List<DailyBudgetResponse> responses = dailyBudgets.stream()
                .map(DailyBudgetResponse::of)
                .toList();

        return ApiResponse.createSuccess(responses);
    }

    @GetMapping("/me/budgets/month/{yearMonth}")
    public ApiResponse<MonthlyBudgetResponse> monthlyBudgetByDate(@UserContext MemberDto memberDto,
                                                                  @PathVariable("yearMonth") String yearMonth) {
        MonthlyBudget monthlyBudget = budgetService.getMonthlyBudgetBy(memberDto.getProfileId(),
                YearMonth.parse(yearMonth));

        return ApiResponse.createSuccess(MonthlyBudgetResponse.of(monthlyBudget));
    }

    @PutMapping("/me/budgets/month/{yearMonth}/default")
    public ApiResponse<Void> registerDefaultMonthlyBudget(@UserContext MemberDto memberDto,
                                                          @PathVariable("yearMonth") String yearMonth,
                                                          @RequestParam("limit") Long limit) {
        budgetService.registerDefaultMonthlyBudgetBy(memberDto.getProfileId(),
                limit, YearMonth.parse(yearMonth));

        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/me/budgets/monthly/{yearMonth}")
    public ApiResponse<Void> editMonthlyBudget(@UserContext MemberDto memberDto,
                                               @PathVariable("yearMonth") String yearMonth,
                                               @RequestParam("limit") Long limit) {
        budgetService.editMonthlyBudgetCustom(memberDto.getProfileId(),
                YearMonth.parse(yearMonth), limit);

        return ApiResponse.createSuccessWithNoContent();
    }


    @AllArgsConstructor
    @Data
    static class MemberProfilePageResponse {
        private String nickName;
        private String email;
        private MemberType memberType;
        private String groupName;
        private String primaryAddress;

        public MemberProfilePageResponse(MemberProfile profile, MemberDto memberDto) {
            this.nickName = profile.getNickName();
            this.email = memberDto.getEmail();
            Address address = profile.findPrimaryAddress().getAddress();
            this.primaryAddress = address.getRoadAddress() + address.getDetailAddress();
            this.memberType = profile.getType();
            this.groupName = profile.getGroup().getName();
        }
    }

    @AllArgsConstructor
    @Data
    static class MemberProfileRequest {
        private String nickName;
        private Long groupId;
        private MemberType memberType;
    }

    @AllArgsConstructor
    @Data
    static class MemberAddressCURequest {
        private String roadAddress;
        private AddressType addressType;
        private String alias;
        private String detailAddress;

        public AddressRequest toAddressApiRequest() {
            return new AddressRequest(roadAddress, detailAddress);
        }
    }

    @AllArgsConstructor
    @Data
    static class PreferencesRequest {
        private List<Long> liked;
        private List<Long> disliked;
    }

    @AllArgsConstructor
    @Data
    static class PreferencesResponse {
        private List<CategoryPreferenceDto> liked;
        private List<CategoryPreferenceDto> disliked;
    }

    @AllArgsConstructor
    @Data
    static class CategoryPreferenceDto {
        private Long categoryId;
        private String categoryName;
        private Integer priority;
    }

    @AllArgsConstructor
    @Data
    static class DailyBudgetResponse {
        private Long dailySpentAmount;
        private Long dailyLimitAmount;
        private Long dailyAvailableAmount;

        public static DailyBudgetResponse of(Budget dailyBudget) {
            return new DailyBudgetResponse(
                    dailyBudget.getSpendAmount().longValue(),
                    dailyBudget.getLimit().longValue(),
                    dailyBudget.getAvailableAmount().longValue()
            );
        }
    }

    @AllArgsConstructor
    @Data
    static class MonthlyBudgetResponse {
        private Long monthlySpentAmount;
        private Long monthlyLimitAmount;
        private Long monthlyAvailableAmount;

        public static MonthlyBudgetResponse of(Budget monthlyBudget) {
            return new MonthlyBudgetResponse(
                    monthlyBudget.getSpendAmount().longValue(),
                    monthlyBudget.getLimit().longValue(),
                    monthlyBudget.getAvailableAmount().longValue()
            );
        }
    }

}
