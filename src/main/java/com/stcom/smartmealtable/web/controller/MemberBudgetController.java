package com.stcom.smartmealtable.web.controller;

import com.stcom.smartmealtable.domain.Budget.Budget;
import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.service.BudgetService;
import com.stcom.smartmealtable.service.dto.MemberDto;
import com.stcom.smartmealtable.web.argumentresolver.UserContext;
import com.stcom.smartmealtable.web.dto.ApiResponse;
import com.stcom.smartmealtable.web.validation.YearMonthFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me/budgets")
public class MemberBudgetController {

    private final BudgetService budgetService;

    // 일별 예산 조회
    @GetMapping("/daily/{date}")
    public ApiResponse<DailyBudgetResponse> dailyBudgetByDate(@UserContext MemberDto memberDto,
                                                              @PathVariable("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        DailyBudget dailyBudget = budgetService.getDailyBudgetBy(memberDto.getProfileId(), date);
        return ApiResponse.createSuccess(DailyBudgetResponse.of(dailyBudget));
    }

    @PutMapping("/daily/{date}/default")
    public ApiResponse<Void> registerDefaultDailyBudget(@UserContext MemberDto memberDto,
                                                        @PathVariable("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date,
                                                        @RequestParam("limit") Long limit) {
        budgetService.registerDefaultDailyBudgetBy(memberDto.getProfileId(), limit, date);
        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/daily/{date}")
    public ApiResponse<Void> editDailyBudget(@UserContext MemberDto memberDto,
                                             @PathVariable("date") @DateTimeFormat(iso = ISO.DATE) String date,
                                             @RequestParam("limit") Long limit) {
        budgetService.editDailyBudgetCustom(memberDto.getProfileId(), LocalDate.parse(date), limit);
        return ApiResponse.createSuccessWithNoContent();
    }

    // 해당 일자가 속한 일일 예산 주간 데이터 조회
    @GetMapping("/daily/{date}/week")
    public ApiResponse<List<DailyBudgetResponse>> dailyBudgetWeekByDate(@UserContext MemberDto memberDto,
                                                                        @PathVariable("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        List<DailyBudget> dailyBudgets = budgetService.getDailyBudgetsByWeek(memberDto.getProfileId(),
                date);

        List<DailyBudgetResponse> responses = dailyBudgets.stream()
                .map(DailyBudgetResponse::of)
                .toList();

        return ApiResponse.createSuccess(responses);
    }

    // 해당 일자가 속한 달을 포함하여, 이전 6개월 조회
    @GetMapping("/montly")
    public ApiResponse<List<MonthlyBudgetResponse>> monthlyBudgetsByDate(@UserContext MemberDto memberDto,
                                                                         @PathVariable("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        List<MonthlyBudget> monthlyBudgets = budgetService.getMonthlyBudgetsBy(memberDto.getProfileId(),
                date, 6);

        List<MonthlyBudgetResponse> responses = monthlyBudgets.stream()
                .map(MonthlyBudgetResponse::of)
                .toList();

        return ApiResponse.createSuccess(responses);
    }

    @GetMapping("/monthly/{yearMonth}")
    public ApiResponse<MonthlyBudgetResponse> monthlyBudgetByDate(@UserContext MemberDto memberDto,
                                                                  @PathVariable("yearMonth") @YearMonthFormat YearMonth yearMonth) {
        MonthlyBudget monthlyBudget = budgetService.getMonthlyBudgetBy(memberDto.getProfileId(),
                yearMonth);

        return ApiResponse.createSuccess(MonthlyBudgetResponse.of(monthlyBudget));
    }

    @PutMapping("/monthly/{yearMonth}/default")
    public ApiResponse<Void> registerDefaultMonthlyBudget(@UserContext MemberDto memberDto,
                                                          @PathVariable("yearMonth") @YearMonthFormat YearMonth yearMonth,
                                                          @RequestParam("limit") Long limit) {
        budgetService.registerDefaultMonthlyBudgetBy(memberDto.getProfileId(),
                limit, yearMonth);

        return ApiResponse.createSuccessWithNoContent();
    }

    @PatchMapping("/monthly/{yearMonth}")
    public ApiResponse<Void> editMonthlyBudget(@UserContext MemberDto memberDto,
                                               @PathVariable("yearMonth") @YearMonthFormat YearMonth yearMonth,
                                               @RequestParam("limit") Long limit) {
        budgetService.editMonthlyBudgetCustom(memberDto.getProfileId(),
                yearMonth, limit);

        return ApiResponse.createSuccessWithNoContent();
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