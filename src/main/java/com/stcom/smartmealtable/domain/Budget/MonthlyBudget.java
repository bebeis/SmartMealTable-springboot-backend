package com.stcom.smartmealtable.domain.Budget;

import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.infrastructure.persistence.YearMonthConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.time.YearMonth;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class MonthlyBudget extends Budget {

    public MonthlyBudget(MemberProfile memberProfile, BigDecimal limit,
                         YearMonth yearMonth) {
        super(memberProfile, limit);
        this.yearMonth = yearMonth;
    }

    @Convert(converter = YearMonthConverter.class)
    @Column(name = "budget_year_month")
    private YearMonth yearMonth;
    
}
