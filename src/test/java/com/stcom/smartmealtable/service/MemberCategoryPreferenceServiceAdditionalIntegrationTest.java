package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.stcom.smartmealtable.domain.food.FoodCategory;
import com.stcom.smartmealtable.domain.food.MemberCategoryPreference;
import com.stcom.smartmealtable.domain.food.PreferenceType;
import com.stcom.smartmealtable.domain.member.Member;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.domain.member.MemberType;
import com.stcom.smartmealtable.repository.FoodCategoryRepository;
import com.stcom.smartmealtable.repository.MemberCategoryPreferenceRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import com.stcom.smartmealtable.repository.MemberRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberCategoryPreferenceServiceAdditionalIntegrationTest {

    @Autowired
    private MemberCategoryPreferenceService preferenceService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberProfileRepository profileRepository;

    @Autowired
    private FoodCategoryRepository categoryRepository;

    @Autowired
    private MemberCategoryPreferenceRepository preferenceRepository;

    private Member member;
    private MemberProfile profile;
    private FoodCategory category1;
    private FoodCategory category2;
    private FoodCategory category3;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .email("preference_test@example.com")
                .rawPassword("password123!")
                .build();
        memberRepository.save(member);

        profile = MemberProfile.builder()
                .member(member)
                .nickName("취향테스터")
                .type(MemberType.STUDENT)
                .build();
        profileRepository.save(profile);

        category1 = new FoodCategory();
        ReflectionTestUtils.setField(category1, "name", "한식");
        categoryRepository.save(category1);

        category2 = new FoodCategory();
        ReflectionTestUtils.setField(category2, "name", "중식");
        categoryRepository.save(category2);

        category3 = new FoodCategory();
        ReflectionTestUtils.setField(category3, "name", "일식");
        categoryRepository.save(category3);
    }

    @Test
    @DisplayName("선호와 비선호 카테고리를 동시에 저장하고 조회할 수 있다")
    void saveBothLikedAndDislikedPreferences() {
        // given
        List<Long> liked = Arrays.asList(category1.getId(), category2.getId());
        List<Long> disliked = Collections.singletonList(category3.getId());

        // when
        preferenceService.savePreferences(profile.getId(), liked, disliked);
        List<MemberCategoryPreference> preferences = preferenceService.getPreferences(profile.getId());

        // then
        assertThat(preferences).hasSize(3);
        
        // 좋아하는 카테고리
        List<MemberCategoryPreference> likedPrefs = preferences.stream()
                .filter(p -> p.getType() == PreferenceType.LIKE)
                .toList();
        assertThat(likedPrefs).hasSize(2);
        assertThat(likedPrefs.get(0).getCategory().getName()).isEqualTo("한식");
        assertThat(likedPrefs.get(1).getCategory().getName()).isEqualTo("중식");

        // 싫어하는 카테고리
        List<MemberCategoryPreference> dislikedPrefs = preferences.stream()
                .filter(p -> p.getType() == PreferenceType.DISLIKE)
                .toList();
        assertThat(dislikedPrefs).hasSize(1);
        assertThat(dislikedPrefs.get(0).getCategory().getName()).isEqualTo("일식");
    }

    @Test
    @DisplayName("존재하지 않는 프로필 ID로 선호도를 저장하면 예외가 발생한다")
    void savePreferencesWithInvalidProfileId() {
        // given
        Long invalidProfileId = 9999L;
        List<Long> liked = Collections.singletonList(category1.getId());
        List<Long> disliked = Collections.emptyList();

        // when & then
        assertThatThrownBy(() -> preferenceService.savePreferences(invalidProfileId, liked, disliked))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 ID로 선호도를 저장하면 예외가 발생한다")
    void savePreferencesWithInvalidCategoryId() {
        // given
        Long invalidCategoryId = 9999L;
        List<Long> liked = Collections.singletonList(invalidCategoryId);
        List<Long> disliked = Collections.emptyList();

        // when & then
        assertThatThrownBy(() -> preferenceService.savePreferences(profile.getId(), liked, disliked))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 카테고리입니다");
    }

    @Test
    @DisplayName("선호도를 업데이트하면 기존 선호도가 모두 삭제되고 새로운 선호도가 저장된다")
    void updatePreferences() {
        // given
        // 초기 선호도 설정
        preferenceService.savePreferences(
                profile.getId(), 
                Collections.singletonList(category1.getId()),
                Collections.singletonList(category2.getId())
        );
        
        // 초기 상태 확인
        List<MemberCategoryPreference> initialPrefs = preferenceService.getPreferences(profile.getId());
        assertThat(initialPrefs).hasSize(2);
        
        // when
        // 선호도 업데이트 - 완전히 반대로 변경
        preferenceService.savePreferences(
                profile.getId(),
                Collections.singletonList(category2.getId()),
                Collections.singletonList(category1.getId())
        );
        
        // then
        List<MemberCategoryPreference> updatedPrefs = preferenceService.getPreferences(profile.getId());
        assertThat(updatedPrefs).hasSize(2);
        
        // 변경된 선호도 확인
        MemberCategoryPreference likedPref = updatedPrefs.stream()
                .filter(p -> p.getType() == PreferenceType.LIKE)
                .findFirst()
                .orElseThrow();
        assertThat(likedPref.getCategory().getId()).isEqualTo(category2.getId());
        
        MemberCategoryPreference dislikedPref = updatedPrefs.stream()
                .filter(p -> p.getType() == PreferenceType.DISLIKE)
                .findFirst()
                .orElseThrow();
        assertThat(dislikedPref.getCategory().getId()).isEqualTo(category1.getId());
    }

    @Test
    @DisplayName("선호도를 비우면 기존 선호도가 모두 삭제된다")
    void clearPreferences() {
        // given
        // 초기 선호도 설정
        preferenceService.savePreferences(
                profile.getId(),
                Arrays.asList(category1.getId(), category2.getId()),
                Collections.singletonList(category3.getId())
        );
        
        // 초기 상태 확인
        List<MemberCategoryPreference> initialPrefs = preferenceService.getPreferences(profile.getId());
        assertThat(initialPrefs).hasSize(3);
        
        // when
        // 선호도 비우기
        preferenceService.savePreferences(
                profile.getId(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        
        // then
        List<MemberCategoryPreference> updatedPrefs = preferenceService.getPreferences(profile.getId());
        assertThat(updatedPrefs).isEmpty();
    }
} 