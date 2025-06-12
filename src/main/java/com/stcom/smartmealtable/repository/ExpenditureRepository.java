package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.Budget.Expenditure;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {

    Slice<Expenditure> findByDailyBudget_MemberProfile_IdOrderBySpentDateDesc(Long profileId, Pageable pageable);
}
