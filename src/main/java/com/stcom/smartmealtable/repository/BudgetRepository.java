package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.Budget.Budget;
import com.stcom.smartmealtable.domain.Budget.DailyBudget;
import com.stcom.smartmealtable.domain.Budget.MonthlyBudget;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("select b from Budget b where type(b) = DailyBudget and b.memberProfile.id = :memberProfileId")
    List<DailyBudget> findDailyBudgetsViaType(@Param("memberProfileId") Long memberProfileId);

    @Query("select b from Budget b where type(b) = DailyBudget and b.memberProfile.id = :memberProfileId order by treat(b as DailyBudget).date desc")
    List<DailyBudget> findFirstDailyBudgetByMemberProfileIdList(@Param("memberProfileId") Long memberProfileId, Pageable pageable);
    
    default Optional<DailyBudget> findFirstDailyBudgetByMemberProfileId(Long memberProfileId) {
        List<DailyBudget> results = findFirstDailyBudgetByMemberProfileIdList(memberProfileId, Pageable.ofSize(1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Query("select b from Budget b where type(b) = MonthlyBudget and b.memberProfile.id = :memberProfileId")
    List<MonthlyBudget> findMonthlyBudgetsViaType(@Param("memberProfileId") Long memberProfileId);

    @Query("select b from Budget b where type(b) = MonthlyBudget and b.memberProfile.id = :memberProfileId order by treat(b as MonthlyBudget).yearMonth desc")
    List<MonthlyBudget> findFirstMonthlyBudgetByMemberProfileIdList(@Param("memberProfileId") Long memberProfileId, Pageable pageable);
    
    default Optional<MonthlyBudget> findFirstMonthlyBudgetByMemberProfileId(Long memberProfileId) {
        List<MonthlyBudget> results = findFirstMonthlyBudgetByMemberProfileIdList(memberProfileId, Pageable.ofSize(1));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Query("select b from Budget b where type(b) = DailyBudget and b.memberProfile.id = :profileId and treat(b as DailyBudget).date = :date")
    Optional<DailyBudget> findDailyBudgetByMemberProfileIdAndDate(Long profileId, LocalDate date);

    @Query("select b from Budget b where type(b) = MonthlyBudget and b.memberProfile.id = :profileId and treat(b as MonthlyBudget).yearMonth = :date")
    Optional<MonthlyBudget> findMonthlyBudgetByMemberProfileIdAndDate(Long profileId, LocalDate date);

    @Query("select b from Budget b where type(b) = MonthlyBudget and b.memberProfile.id = :profileId and treat(b as MonthlyBudget).yearMonth = :yearMonth")
    Optional<MonthlyBudget> findMonthlyBudgetByMemberProfileIdAndYearMonth(Long profileId, YearMonth yearMonth);

    @Query("select b from Budget b where type(b) = DailyBudget and b.memberProfile.id = :profileId and treat(b as DailyBudget).date between :startOfWeek and :endOfWeek")
    List<DailyBudget> findDailyBudgetsByMemberProfileIdAndDateBetween(Long profileId, LocalDate startOfWeek,
                                                                      LocalDate endOfWeek);
}
