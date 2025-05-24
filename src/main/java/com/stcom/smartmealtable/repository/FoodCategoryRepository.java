package com.stcom.smartmealtable.repository;

import com.stcom.smartmealtable.domain.food.FoodCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodCategoryRepository extends JpaRepository<FoodCategory, Long> {
} 