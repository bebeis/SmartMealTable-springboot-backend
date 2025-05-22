package com.stcom.smartmealtable.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.stcom.smartmealtable.domain.food.FoodCategory;
import com.stcom.smartmealtable.domain.food.MemberCategoryPreference;
import com.stcom.smartmealtable.domain.food.PreferenceType;
import com.stcom.smartmealtable.domain.member.MemberProfile;
import com.stcom.smartmealtable.repository.FoodCategoryRepository;
import com.stcom.smartmealtable.repository.MemberCategoryPreferenceRepository;
import com.stcom.smartmealtable.repository.MemberProfileRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberCategoryPreferenceServiceTest {

    @Mock
    private MemberCategoryPreferenceRepository preferenceRepository;

    @Mock
    private FoodCategoryRepository categoryRepository;

    @Mock
    private MemberProfileRepository profileRepository;

    @InjectMocks
    private MemberCategoryPreferenceService preferenceService;
    
    @Captor
    private ArgumentCaptor<MemberCategoryPreference> preferenceCaptor;

    private MemberProfile profile;
    private FoodCategory koreanFood;
    private FoodCategory westernFood;
    private FoodCategory japaneseFood;
    private FoodCategory chineseFood;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 셋업
        profile = new MemberProfile();
        ReflectionTestUtils.setField(profile, "id", 1L);
        
        koreanFood = createFoodCategory(1L, "한식");
        westernFood = createFoodCategory(2L, "양식");
        japaneseFood = createFoodCategory(3L, "일식");
        chineseFood = createFoodCategory(4L, "중식");
    }

    @Test
    @DisplayName("회원 음식 선호도를 저장할 수 있어야 한다")
    void savePreferences() {
        // given
        Long profileId = 1L;
        List<Long> liked = Arrays.asList(1L, 2L);  // 한식, 양식 선호
        List<Long> disliked = Arrays.asList(3L);   // 일식 비선호
        
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(koreanFood));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(westernFood));
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(japaneseFood));
        doNothing().when(preferenceRepository).deleteByMemberProfile_Id(profileId);
        when(preferenceRepository.save(any(MemberCategoryPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        preferenceService.savePreferences(profileId, liked, disliked);

        // then
        verify(profileRepository, times(1)).findById(profileId);
        verify(preferenceRepository, times(1)).deleteByMemberProfile_Id(profileId);
        verify(categoryRepository, times(3)).findById(anyLong());
        verify(preferenceRepository, times(3)).save(preferenceCaptor.capture());
        
        List<MemberCategoryPreference> savedPreferences = preferenceCaptor.getAllValues();
        assertThat(savedPreferences).hasSize(3);
        
        // 선호 음식 검증
        assertThat(savedPreferences.get(0).getType()).isEqualTo(PreferenceType.LIKE);
        assertThat(savedPreferences.get(0).getCategory().getName()).isEqualTo("한식");
        assertThat(savedPreferences.get(0).getPriority()).isEqualTo(1);
        
        assertThat(savedPreferences.get(1).getType()).isEqualTo(PreferenceType.LIKE);
        assertThat(savedPreferences.get(1).getCategory().getName()).isEqualTo("양식");
        assertThat(savedPreferences.get(1).getPriority()).isEqualTo(2);
        
        // 비선호 음식 검증
        assertThat(savedPreferences.get(2).getType()).isEqualTo(PreferenceType.DISLIKE);
        assertThat(savedPreferences.get(2).getCategory().getName()).isEqualTo("일식");
        assertThat(savedPreferences.get(2).getPriority()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("존재하지 않는 프로필로 음식 선호도 저장시 예외가 발생해야 한다")
    void savePreferencesWithNonExistingProfile() {
        // given
        Long profileId = 999L;
        List<Long> liked = Arrays.asList(1L);
        List<Long> disliked = List.of();
        
        when(profileRepository.findById(profileId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> preferenceService.savePreferences(profileId, liked, disliked))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("존재하지 않는 프로필입니다");
    }
    
    @Test
    @DisplayName("존재하지 않는 카테고리로 음식 선호도 저장시 예외가 발생해야 한다")
    void savePreferencesWithNonExistingCategory() {
        // given
        Long profileId = 1L;
        List<Long> liked = Arrays.asList(999L);
        List<Long> disliked = List.of();
        
        when(profileRepository.findById(profileId)).thenReturn(Optional.of(profile));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());
        doNothing().when(preferenceRepository).deleteByMemberProfile_Id(profileId);

        // when & then
        assertThatThrownBy(() -> preferenceService.savePreferences(profileId, liked, disliked))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("존재하지 않는 카테고리입니다");
    }
    
    @Test
    @DisplayName("프로필 ID로 음식 선호도를 조회할 수 있어야 한다")
    void getPreferences() {
        // given
        Long profileId = 1L;
        MemberCategoryPreference pref1 = createPreference(profile, koreanFood, PreferenceType.LIKE, 1);
        MemberCategoryPreference pref2 = createPreference(profile, westernFood, PreferenceType.LIKE, 2);
        MemberCategoryPreference pref3 = createPreference(profile, japaneseFood, PreferenceType.DISLIKE, 1);
        
        List<MemberCategoryPreference> expectedPreferences = Arrays.asList(pref1, pref2, pref3);
        
        when(preferenceRepository.findDefaultByMemberProfileId(profileId)).thenReturn(expectedPreferences);

        // when
        List<MemberCategoryPreference> foundPreferences = preferenceService.getPreferences(profileId);

        // then
        assertThat(foundPreferences).hasSize(3);
        assertThat(foundPreferences).isEqualTo(expectedPreferences);
        
        verify(preferenceRepository, times(1)).findDefaultByMemberProfileId(profileId);
    }
    
    private FoodCategory createFoodCategory(Long id, String name) {
        FoodCategory foodCategory = new FoodCategory();
        ReflectionTestUtils.setField(foodCategory, "id", id);
        ReflectionTestUtils.setField(foodCategory, "name", name);
        return foodCategory;
    }
    
    private MemberCategoryPreference createPreference(MemberProfile profile, FoodCategory category, 
                                                     PreferenceType type, Integer priority) {
        MemberCategoryPreference preference = MemberCategoryPreference.builder()
                .memberProfile(profile)
                .category(category)
                .type(type)
                .priority(priority)
                .build();
        ReflectionTestUtils.setField(preference, "id", Long.valueOf(priority));
        return preference;
    }
} 