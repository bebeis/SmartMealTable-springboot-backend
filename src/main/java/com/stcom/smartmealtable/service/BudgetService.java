package com.stcom.smartmealtable.service;

import static java.time.DayOfWeek.MONDAY;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.BudgetRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final MemberProfileRepository memberProfileRepository;

    public DailyBudget findRecentDailyBudgetByMemberProfileId(Long memberProfileId) {
        return budgetRepository.findFirstDailyBudgetByMemberProfileId(memberProfileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필로 접근"));
    }

    public MonthlyBudget findRecentMonthlyBudgetByMemberProfileId(Long memberProfileId) {
        return budgetRepository.findFirstMonthlyBudgetByMemberProfileId(memberProfileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필로 접근"));
    }

    @Transactional
    public void saveMonthlyBudgetCustom(Long memberProfileId, Long limit) {
        MemberProfile profile = memberProfileRepository.findById(memberProfileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필로 접근"));
        MonthlyBudget monthlyBudget = new MonthlyBudget(profile, BigDecimal.valueOf(limit), YearMonth.now());
        budgetRepository.save(monthlyBudget);
    }

    @Transactional
    public void saveDailyBudgetCustom(Long memberProfileId, Long limit) {
        MemberProfile profile = memberProfileRepository.findById(memberProfileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필로 접근"));
        DailyBudget dailyBudget = new DailyBudget(profile, BigDecimal.valueOf(limit), LocalDate.now());
        budgetRepository.save(dailyBudget);
    }


    public DailyBudget getDailyBudgetBy(Long profileId, LocalDate date) {
        return budgetRepository.findDailyBudgetByMemberProfileIdAndDate(profileId, date).orElseThrow(() ->
                new IllegalArgumentException("예산이 존재하지 않습니다.")
        );
    }

    @Transactional
    public void registerDefaultDailyBudgetBy(Long profileId, Long dailyLimit, LocalDate startDate) {
        MemberProfile profile = memberProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필로 접근"));
        // 일일 예산은 오늘 ~ 이번달 말일까지 디폴트 daily Limit로 여러 개 생성해준다.
        List<DailyBudget> budgets = new ArrayList<>();
        for (LocalDate date = startDate; date.getMonth() == startDate.getMonth(); date = date.plusDays(1)) {
            budgets.add(new DailyBudget(profile, BigDecimal.valueOf(dailyLimit), date));
        }
        budgetRepository.saveAll(budgets);
    }

    @Transactional
    public void registerDefaultMonthlyBudgetBy(Long profileId, Long monthlyLimit,
                                               YearMonth startYearMonth) {

        MemberProfile profile = memberProfileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필로 접근"));
        // 월간 예산은 이번달에 한 번만 생성해준다.
        MonthlyBudget monthlyBudget = new MonthlyBudget(profile, BigDecimal.valueOf(monthlyLimit), startYearMonth);
        budgetRepository.save(monthlyBudget);
    }

    public MonthlyBudget getMonthlyBudgetBy(Long profileId, YearMonth yearMonth) {
        return budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(profileId, yearMonth)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필로 접근"));
    }

    public List<DailyBudget> getDailyBudgetsByWeek(Long profileId, LocalDate date) {
        LocalDate startOfWeek = date.with(MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return budgetRepository.findDailyBudgetsByMemberProfileIdAndDateBetween(profileId, startOfWeek, endOfWeek);
    }

    @Transactional
    public void editMonthlyBudgetCustom(Long profileId, YearMonth yearMonth, Long limit) {
        budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(profileId,
                        yearMonth)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필로 접근"))
                .changeLimit(BigDecimal.valueOf(limit));
    }

    @Transactional
    public void editDailyBudgetCustom(Long profileId, LocalDate date, Long limit) {
        budgetRepository.findDailyBudgetByMemberProfileIdAndDate(profileId, date)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필로 접근"))
                .changeLimit(BigDecimal.valueOf(limit));
    }
}
