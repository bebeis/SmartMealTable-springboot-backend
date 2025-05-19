package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.food.FoodPreference;
import com.stcom.smartmealtable.domain.member.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodPreferenceRepository extends JpaRepository<FoodPreference, Long> {

    List<FoodPreference> findFoodPreferencesByMember(Member member);
}
