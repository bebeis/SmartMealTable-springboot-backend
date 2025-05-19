package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public DailyBudget findRecentDailyBudgetByMemberId(Long memberId) {
        return budgetRepository.findFirstDailyBudgetByMemberId(memberId).orElse(null);
    }

    public MonthlyBudget findRecentMonthlyBudgetByMemberId(Long memberId) {
        return budgetRepository.findFirstMonthlyBudgetByMemberId(memberId).orElse(null);
    }

}
