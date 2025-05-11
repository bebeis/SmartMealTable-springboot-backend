package com.stcom.smartmealtable.domain.Budget;

import jakarta.persistence.Entity;
import java.time.LocalDate;
import lombok.Getter;

@Entity
@Getter
public class DailyBudget extends Budget {

    private LocalDate localDate;
}
