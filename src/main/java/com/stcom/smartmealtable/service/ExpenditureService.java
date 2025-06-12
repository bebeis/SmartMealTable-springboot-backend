package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.Expenditure;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import com.stcom.smartmealtable.repository.BudgetRepository;
import com.stcom.smartmealtable.repository.ExpenditureRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenditureService {

    private final ExpenditureRepository expenditureRepository;
    private final BudgetRepository budgetRepository;

    @Transactional
    public void registerExpenditure(Long profileId,
                                    LocalDateTime spentDate,
                                    Long amount,
                                    String tradeName) {

        LocalDate date = spentDate.toLocalDate();
        YearMonth yearMonth = YearMonth.from(spentDate);

        DailyBudget dailyBudget = budgetRepository.findDailyBudgetByMemberProfileIdAndDate(profileId, date)
                .orElseThrow(() -> new IllegalArgumentException("일일 예산이 존재하지 않습니다."));
        MonthlyBudget monthlyBudget = budgetRepository.findMonthlyBudgetByMemberProfileIdAndYearMonth(profileId,
                        yearMonth)
                .orElseThrow(() -> new IllegalArgumentException("월별 예산이 존재하지 않습니다."));

        Expenditure expenditure = Expenditure.builder()
                .spentDate(spentDate)
                .amount(amount)
                .tradeName(tradeName)
                .dailyBudget(dailyBudget)
                .monthlyBudget(monthlyBudget)
                .build();
        expenditureRepository.save(expenditure);

        BigDecimal spent = BigDecimal.valueOf(amount);
        dailyBudget.addSpent(spent);
        monthlyBudget.addSpent(spent);
    }

    public Slice<Expenditure> getExpenditures(Long profileId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "spentDate"));
        return expenditureRepository.findByDailyBudget_MemberProfile_IdOrderBySpentDateDesc(profileId, pageable);
    }

    @Transactional
    public void editExpenditure(Long profileId, Long expenditureId, LocalDateTime spentDate, Long amount,
                                String tradeName) {
        Expenditure expenditure = expenditureRepository.findById(expenditureId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역이 존재하지 않습니다."));

        if (!expenditure.getDailyBudget().getMemberProfile().getId().equals(profileId)) {
            throw new IllegalArgumentException("해당 지출 내역 등록자와 접근자가 다릅니다.");
        }
        
        expenditure.edit(spentDate, amount, tradeName);
    }

    @Transactional
    public void deleteExpenditure(Long profileId, Long expenditureId) {
        Expenditure expenditure = expenditureRepository.findById(expenditureId)
                .orElseThrow(() -> new IllegalArgumentException("지출 내역이 존재하지 않습니다."));

        DailyBudget dailyBudget = expenditure.getDailyBudget();
        MonthlyBudget monthlyBudget = expenditure.getMonthlyBudget();

        if (!dailyBudget.getMemberProfile().getId().equals(profileId)) {
            throw new IllegalArgumentException("해당 지출 내역 등록자와 접근자가 다릅니다.");
        }

        BigDecimal spent = BigDecimal.valueOf(expenditure.getAmount());
        dailyBudget.subtractSpent(spent);
        monthlyBudget.subtractSpent(spent);

        expenditureRepository.delete(expenditure);
    }
}
