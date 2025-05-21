package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.food.FoodPreference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FoodPreferenceRepository extends JpaRepository<FoodPreference, Long> {

    @Query("select fp from FoodPreference fp where fp.memberProfile.id = :memberProfileId")
    List<FoodPreference> findFoodPreferencesByMemberProfileId(@Param("memberProfileId") Long memberProfileId);
}
