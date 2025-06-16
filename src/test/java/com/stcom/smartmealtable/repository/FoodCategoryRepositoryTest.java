package com.stcom.smartmealtable.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.stcom.smartmealtable.domain.food.FoodCategory;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
class FoodCategoryRepositoryTest {

    @Autowired
    private FoodCategoryRepository repository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("음식 카테고리를 저장하고 ID 로 조회할 수 있다")
    void saveAndFind() {
        // given
        FoodCategory category = new FoodCategory();
        ReflectionTestUtils.setField(category, "name", "한식");
        repository.save(category);
        em.flush();
        em.clear();

        // when
        Optional<FoodCategory> found = repository.findById(category.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("한식");
    }
} 