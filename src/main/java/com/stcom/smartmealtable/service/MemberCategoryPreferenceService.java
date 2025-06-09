package com.stcom.smartmealtable.service;

import com.stcom.smartmealtable.domain.food.FoodCategory;
import com.stcom.smartmealtable.domain.food.MemberCategoryPreference;
import com.stcom.smartmealtable.domain.food.PreferenceType;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.FoodCategoryRepository;
import com.stcom.smartmealtable.repository.MemberCategoryPreferenceRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberCategoryPreferenceService {

    private final MemberCategoryPreferenceRepository preferenceRepository;
    private final FoodCategoryRepository categoryRepository;
    private final MemberProfileRepository profileRepository;

    @Transactional
    public void savePreferences(Long profileId, List<Long> liked, List<Long> disliked) {
        MemberProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 프로필입니다"));

        preferenceRepository.deleteByMemberProfile_Id(profileId);

        int priority = 1;
        for (Long catId : liked) {
            FoodCategory category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + catId));
            MemberCategoryPreference pref = MemberCategoryPreference.builder()
                    .memberProfile(profile)
                    .category(category)
                    .type(PreferenceType.LIKE)
                    .priority(priority++)
                    .build();
            preferenceRepository.save(pref);
        }

        priority = 1;
        for (Long catId : disliked) {
            FoodCategory category = categoryRepository.findById(catId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + catId));
            MemberCategoryPreference pref = MemberCategoryPreference.builder()
                    .memberProfile(profile)
                    .category(category)
                    .type(PreferenceType.DISLIKE)
                    .priority(priority++)
                    .build();
            preferenceRepository.save(pref);
        }
    }

    public List<MemberCategoryPreference> getPreferences(Long profileId) {
        return preferenceRepository.findDefaultByMemberProfileId(profileId);
    }
} 