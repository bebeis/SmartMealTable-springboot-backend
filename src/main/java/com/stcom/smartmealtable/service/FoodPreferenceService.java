package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.food.FoodCategory;
import com.stcom.smartmealtable.domain.food.FoodPreference;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.repository.FoodPreferenceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FoodPreferenceService {

    private final FoodPreferenceRepository foodPreferenceRepository;

    public List<FoodCategory> findPreferredFoodCategories(Member member) {
        return foodPreferenceRepository.findFoodPreferencesByMember(member)
                .stream()
                .map(FoodPreference::getCategory)
                .toList();
    }
}
