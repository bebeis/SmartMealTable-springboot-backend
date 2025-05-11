package com.stcom.smartmealtable.domain.Budget;

import com.stcom.smartmealtable.domain.member.MemberAuth;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.YearMonth;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MonthlyBudget extends Budget {

    public MonthlyBudget(MemberAuth memberAuth, BigDecimal limit,
                         YearMonth yearMonth) {
        super(memberAuth, limit);
        this.yearMonth = yearMonth;
    }
    
    private YearMonth yearMonth;
}
