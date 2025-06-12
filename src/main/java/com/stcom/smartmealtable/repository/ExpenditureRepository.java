package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.Budget.Expenditure;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenditureRepository extends JpaRepository<Expenditure, Long> {

    Slice<Expenditure> findByDailyBudget_MemberProfile_IdOrderBySpentDateDesc(Long profileId, Pageable pageable);

    List<Expenditure> findExpendituresById(Long id);
}
