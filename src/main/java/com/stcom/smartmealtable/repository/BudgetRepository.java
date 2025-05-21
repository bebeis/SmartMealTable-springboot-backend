package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.Budget.Budget;
import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("select b from Budget b where type(b) = DailyBudget and b.memberProfile.id = :memberProfileId")
    List<DailyBudget> findDailyBudgetsViaType(@Param("memberProfileId") Long memberProfileId);

    @Query("select b from Budget b where type(b) = DailyBudget and b.memberProfile.id = :memberProfileId order by treat(b as DailyBudget).date desc")
    Optional<DailyBudget> findFirstDailyBudgetByMemberProfileId(@Param("memberProfileId") Long memberProfileId);

    @Query("select b from Budget b where type(b) = MonthlyBudget and b.memberProfile.id = :memberProfileId")
    List<MonthlyBudget> findMonthlyBudgetsViaType(@Param("memberProfileId") Long memberProfileId);

    @Query("select b from Budget b where type(b) = MonthlyBudget and b.memberProfile.id = :memberProfileId order by treat(b as MonthlyBudget).yearMonth desc")
    Optional<MonthlyBudget> findFirstMonthlyBudgetByMemberProfileId(@Param("memberProfileId") Long memberProfileId);
}
