package com.stcom.smartmealtable.domain.Budget;

import jakarta.persistence.Entity;
import java.time.YearMonth;
import lombok.Getter;

@Entity
@Getter
public class MonthlyBudget extends Budget {

    private YearMonth yearMonth;
}
