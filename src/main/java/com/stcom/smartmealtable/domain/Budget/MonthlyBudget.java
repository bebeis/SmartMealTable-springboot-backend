package com.stcom.smartmealtable.domain.Budget;

import com.stcom.smartmealtable.domain.member.Member;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.YearMonth;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MonthlyBudget extends Budget {

    public MonthlyBudget(Member member, BigDecimal limit,
                         YearMonth yearMonth) {
        super(member, limit);
        this.yearMonth = yearMonth;
    }

    private YearMonth yearMonth;
}
