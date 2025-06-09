package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.BudgetRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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
}
