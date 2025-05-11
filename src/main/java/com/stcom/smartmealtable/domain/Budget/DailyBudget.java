package com.stcom.smartmealtable.domain.Budget;

import com.stcom.smartmealtable.domain.member.MemberAuth;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class DailyBudget extends Budget {

    public DailyBudget(MemberAuth memberAuth, BigDecimal limit,
                       LocalDate date) {
        super(memberAuth, limit);
        this.date = date;
    }

    private LocalDate date;
}
