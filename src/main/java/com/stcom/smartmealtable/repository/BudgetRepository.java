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

    @Query("select b from Budget b where type(b) = DailyBudget and b.member.id = :memberId")
    List<DailyBudget> findDailyBudgetsViaType(@Param("memberId") Long memberId);

    @Query("select b from Budget b where type(b) = DailyBudget and b.member.id = :memberId order by treat (b as DailyBudget).date desc")
    Optional<DailyBudget> findFirstDailyBudgetByMemberId(@Param("memberId") Long memberId);

    @Query("select b from Budget b where type(b) = MonthlyBudget and b.member.id = :memberId")
    List<MonthlyBudget> findMonthlyBudgetsViaType(@Param("memberId") Long memberId);

    @Query("select b from Budget b where type(b) = MonthlyBudget and b.member.id = :memberId order by treat (b as MonthlyBudget ).yearMonth desc")
    Optional<MonthlyBudget> findFirstMonthlyBudgetByMemberId(@Param("memberId") Long memberId);
}
