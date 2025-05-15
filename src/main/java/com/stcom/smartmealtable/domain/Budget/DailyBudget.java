package com.stcom.smartmealtable.domain.Budget;

import com.stcom.smartmealtable.domain.member.Member;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

//@Entity
@Getter
@NoArgsConstructor
public class DailyBudget extends Budget {

    public DailyBudget(Member member, BigDecimal limit,
                       LocalDate date) {
        super(member, limit);
        this.date = date;
    }

    private LocalDate date;
}
