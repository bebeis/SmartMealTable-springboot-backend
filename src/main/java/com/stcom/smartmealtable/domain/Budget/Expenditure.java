package com.stcom.smartmealtable.domain.Budget;

import com.stcom.smartmealtable.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Expenditure extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expenditure_id")
    private Long id;

    private LocalDateTime spentDate;

    private Long amount;

    private String tradeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "daily_budget_id")
    private DailyBudget dailyBudget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_budget_id")
    private MonthlyBudget monthlyBudget;


    @Builder
    public Expenditure(LocalDateTime spentDate, Long amount, String tradeName, DailyBudget dailyBudget,
                       MonthlyBudget monthlyBudget) {
        this.spentDate = spentDate;
        this.amount = amount;
        this.tradeName = tradeName;
        this.dailyBudget = dailyBudget;
        this.monthlyBudget = monthlyBudget;
    }

    private void updateSpentDate(LocalDateTime spentDate) {
        this.spentDate = spentDate;
    }

    private void updateAmount(Long originAmount, Long afterAmount) {
        dailyBudget.addSpent(afterAmount - originAmount);
        monthlyBudget.addSpent(afterAmount - originAmount);
        this.amount = afterAmount;
    }

    private void updateTradeName(String tradeName) {
        this.tradeName = tradeName;
    }

    public void edit(LocalDateTime spentDate, Long amount, String tradeName) {
        updateSpentDate(spentDate);
        updateAmount(this.amount, amount);
        updateTradeName(tradeName);
    }
}
