package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetRepository budgetRepository;

    public DailyBudget findRecentDailyBudgetByMemberProfileId(Long memberProfileId) {
        return budgetRepository.findFirstDailyBudgetByMemberProfileId(memberProfileId).orElse(null);
    }

    public MonthlyBudget findRecentMonthlyBudgetByMemberProfileId(Long memberProfileId) {
        return budgetRepository.findFirstMonthlyBudgetByMemberProfileId(memberProfileId).orElse(null);
    }

}
