package com.stcom.smartmealtable.domain.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.stcom.smartmealtable.domain.member.MemberProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MemberCategoryPreferenceTest {

    @Test
    @DisplayName("MemberCategoryPreference 객체가 빌더를 통해 올바르게 생성된다")
    void createMemberCategoryPreferenceWithBuilder() {
        // given
        MemberProfile memberProfile = new MemberProfile();
        FoodCategory foodCategory = new FoodCategory();
        ReflectionTestUtils.setField(foodCategory, "id", 1L);
        ReflectionTestUtils.setField(foodCategory, "name", "한식");
        
        // when
        MemberCategoryPreference preference = MemberCategoryPreference.builder()
                .memberProfile(memberProfile)
                .category(foodCategory)
                .type(PreferenceType.LIKE)
                .priority(1)
                .build();
        
        // then
        assertThat(preference.getMemberProfile()).isEqualTo(memberProfile);
        assertThat(preference.getCategory()).isEqualTo(foodCategory);
        assertThat(preference.getCategory().getName()).isEqualTo("한식");
        assertThat(preference.getType()).isEqualTo(PreferenceType.LIKE);
        assertThat(preference.getPriority()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("선호도와 비선호도를 표현할 수 있다")
    void expressLikeAndDislikePreferences() {
        // given
        MemberProfile memberProfile = new MemberProfile();
        FoodCategory koreanFood = new FoodCategory();
        FoodCategory japaneseFood = new FoodCategory();
        
        ReflectionTestUtils.setField(koreanFood, "name", "한식");
        ReflectionTestUtils.setField(japaneseFood, "name", "일식");
        
        // when
        MemberCategoryPreference likePreference = MemberCategoryPreference.builder()
                .memberProfile(memberProfile)
                .category(koreanFood)
                .type(PreferenceType.LIKE)
                .priority(1)
                .build();
                
        MemberCategoryPreference dislikePreference = MemberCategoryPreference.builder()
                .memberProfile(memberProfile)
                .category(japaneseFood)
                .type(PreferenceType.DISLIKE)
                .priority(1)
                .build();
        
        // then
        assertThat(likePreference.getType()).isEqualTo(PreferenceType.LIKE);
        assertThat(likePreference.getCategory().getName()).isEqualTo("한식");
        
        assertThat(dislikePreference.getType()).isEqualTo(PreferenceType.DISLIKE);
        assertThat(dislikePreference.getCategory().getName()).isEqualTo("일식");
    }
    
    @Test
    @DisplayName("선호도에 우선순위를 부여할 수 있다")
    void assignPriorityToPreferences() {
        // given
        MemberProfile memberProfile = new MemberProfile();
        FoodCategory koreanFood = new FoodCategory();
        FoodCategory chineseFood = new FoodCategory();
        FoodCategory westernFood = new FoodCategory();
        
        // when
        MemberCategoryPreference firstPreference = MemberCategoryPreference.builder()
                .memberProfile(memberProfile)
                .category(koreanFood)
                .type(PreferenceType.LIKE)
                .priority(1)
                .build();
                
        MemberCategoryPreference secondPreference = MemberCategoryPreference.builder()
                .memberProfile(memberProfile)
                .category(chineseFood)
                .type(PreferenceType.LIKE)
                .priority(2)
                .build();
                
        MemberCategoryPreference thirdPreference = MemberCategoryPreference.builder()
                .memberProfile(memberProfile)
                .category(westernFood)
                .type(PreferenceType.LIKE)
                .priority(3)
                .build();
        
        // then
        assertThat(firstPreference.getPriority()).isLessThan(secondPreference.getPriority());
        assertThat(secondPreference.getPriority()).isLessThan(thirdPreference.getPriority());
        assertThat(firstPreference.getPriority()).isEqualTo(1);
        assertThat(secondPreference.getPriority()).isEqualTo(2);
        assertThat(thirdPreference.getPriority()).isEqualTo(3);
    }
} 