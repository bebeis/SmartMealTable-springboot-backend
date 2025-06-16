package com.stcom.smartmealtable.domain.Budget;

import com.stcom.smartmealtable.domain.member.MemberProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class DailyBudget extends Budget {

    public DailyBudget(MemberProfile memberProfile, BigDecimal limit,
                       LocalDate date) {
        super(memberProfile, limit);
        this.date = date;
    }

    @Column(name = "daily_budget_date")
    private LocalDate date;
    
}
