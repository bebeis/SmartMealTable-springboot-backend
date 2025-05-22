package com.stcom.smartmealtable.domain.food;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class FoodCategoryTest {

    @Test
    @DisplayName("FoodCategory 객체의 필드값이 올바르게 설정된다")
    void foodCategoryFieldsAreSetCorrectly() {
        // given
        FoodCategory foodCategory = new FoodCategory();
        
        // when
        ReflectionTestUtils.setField(foodCategory, "id", 1L);
        ReflectionTestUtils.setField(foodCategory, "name", "한식");
        
        // then
        assertThat(foodCategory.getId()).isEqualTo(1L);
        assertThat(foodCategory.getName()).isEqualTo("한식");
    }
    
    @Test
    @DisplayName("다양한 음식 카테고리를 생성하고 구분할 수 있다")
    void createAndDistinguishMultipleFoodCategories() {
        // given
        FoodCategory koreanFood = new FoodCategory();
        FoodCategory chineseFood = new FoodCategory();
        FoodCategory japaneseFood = new FoodCategory();
        FoodCategory westernFood = new FoodCategory();
        
        // when
        ReflectionTestUtils.setField(koreanFood, "id", 1L);
        ReflectionTestUtils.setField(koreanFood, "name", "한식");
        
        ReflectionTestUtils.setField(chineseFood, "id", 2L);
        ReflectionTestUtils.setField(chineseFood, "name", "중식");
        
        ReflectionTestUtils.setField(japaneseFood, "id", 3L);
        ReflectionTestUtils.setField(japaneseFood, "name", "일식");
        
        ReflectionTestUtils.setField(westernFood, "id", 4L);
        ReflectionTestUtils.setField(westernFood, "name", "양식");
        
        // then
        assertThat(koreanFood.getId()).isNotEqualTo(chineseFood.getId());
        assertThat(koreanFood.getName()).isEqualTo("한식");
        assertThat(chineseFood.getName()).isEqualTo("중식");
        assertThat(japaneseFood.getName()).isEqualTo("일식");
        assertThat(westernFood.getName()).isEqualTo("양식");
    }
} 